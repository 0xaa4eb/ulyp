package com.ulyp.storage.impl;

import com.ulyp.core.*;
import com.ulyp.core.mem.BinaryList;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.repository.InMemoryRepository;
import com.ulyp.core.repository.Repository;
import com.ulyp.core.util.Backoff;
import com.ulyp.core.util.FixedDelayBackoff;
import com.ulyp.storage.*;
import com.ulyp.storage.impl.util.BinaryListFileReader;
import com.ulyp.core.util.NamedThreadFactory;
import com.ulyp.storage.tree.Index;
import com.ulyp.storage.tree.Recording;
import com.ulyp.storage.tree.RecordingState;
import com.ulyp.transport.BinaryProcessMetadataDecoder;
import com.ulyp.transport.BinaryRecordingMetadataDecoder;
import lombok.Getter;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class AsyncFileRecordingDataReader implements RecordingDataReader {

    private final File file;
    private final RecordedMethodCallDataReader recordedMethodCallDataReader;
    private final ReaderSettings settings;
    private final ExecutorService executorService;
    @Getter
    private final CompletableFuture<ProcessMetadata> processMetadataFuture = new CompletableFuture<>();
    private final CompletableFuture<Boolean> finishedReadingFuture = new CompletableFuture<>();
    private final Index index;
    @Getter
    private final InMemoryRepository<Integer, Type> types = new InMemoryRepository<>();
    private final InMemoryRepository<Integer, RecordingState> recordings = new InMemoryRepository<>();
    private final Repository<Integer, Method> methods = new InMemoryRepository<>();
    private volatile StorageReaderTask readingTask;
    private volatile RecordingListener recordingListener = RecordingListener.empty();

    public AsyncFileRecordingDataReader(ReaderSettings settings) {
        this.file = settings.getFile();
        this.index = settings.getIndexSupplier().get();
        this.settings = settings;
        this.recordedMethodCallDataReader = new RecordedMethodCallDataReader(file);
        this.executorService = Executors.newFixedThreadPool(
                5,
                NamedThreadFactory.builder()
                        .name("Reader-" + file.toString())
                        .daemon(true)
                        .build()
        );

        if (settings.isAutoStartReading()) {
            start();
        }
    }

    public synchronized void start() {
        try {
            readingTask = new StorageReaderTask(file, settings);
            executorService.submit(readingTask);
        } catch (IOException e) {
            throw new StorageException("Could not start reader task for file " + file, e);
        }
    }

    @Override
    public CompletableFuture<Void> submitJob(RecordingDataReaderJob job) {
        try {
            return CompletableFuture.runAsync(new JobRunnerTask(job, file), executorService);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RecordedEnterMethodCall readEnterMethodCall(long address) {
        return recordedMethodCallDataReader.readEnterMethodCall(address);
    }

    @Override
    public RecordedExitMethodCall readExitMethodCall(long address) {
        return recordedMethodCallDataReader.readExitMethodCall(address);
    }

    @Override
    public CompletableFuture<Boolean> getFinishedReadingFuture() {
        return finishedReadingFuture;
    }

    @Override
    public void subscribe(RecordingListener listener) {
        this.recordingListener = listener;
    }

    @Override
    public List<Recording> getRecordings() {
        return recordings.values()
                .stream()
                .filter(RecordingState::isPublished)
                .map(Recording::new)
                .collect(Collectors.toList());
    }

    @Override
    public void close() throws StorageException {
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // nop
        }
        StorageReaderTask readingTaskLocal = readingTask;
        if (readingTaskLocal != null) {
            readingTaskLocal.close();
        }
    }

    @Override
    public CompletableFuture<Void> initiateSearch(SearchQuery query, SearchResultListener listener) {
        try {
            return CompletableFuture.runAsync(new SearchTask(file, query, listener), executorService);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    private class JobRunnerTask implements Runnable, Closeable {

        private final BinaryListFileReader reader;
        private final RecordingDataReaderJob job;

        private JobRunnerTask(RecordingDataReaderJob job, File file) throws IOException {
            this.reader = new BinaryListFileReader(file);
            this.job = job;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {

                try {
                    BinaryListWithAddress data = this.reader.readWithAddress();

                    if (data == null) {
                        if (job.continueOnNoData()) {
                            continue;
                        } else {
                            job.onComplete();
                            return;
                        }
                    }

                    switch (data.getBytes().id()) {
                        case ProcessMetadata.WIRE_ID:
                            onProcessMetadata(data.getBytes());
                            break;
                        case RecordingMetadata.WIRE_ID:
                            onRecordingMetadata(data.getBytes());
                            break;
                        case TypeList.WIRE_ID:
                            onTypes(data.getBytes());
                            break;
                        case MethodList.WIRE_ID:
                            onMethods(data.getBytes());
                            break;
                        case RecordedMethodCallList.WIRE_ID:
                            onRecordedCalls(data);
                            break;
                        case RecordingCompleteMark.WIRE_ID:
                            job.onComplete();
                            return;
                        default:
                            throw new StorageException("Unknown binary data id " + data.getBytes().id());
                    }
                } catch (IOException err) {
                    throw new RuntimeException(err);
                }
            }
        }

        @Override
        public void close() {
            try {
                this.reader.close();
            } catch (IOException e) {
                // TODO log only
                throw new RuntimeException(e);
            }
        }

        private void onProcessMetadata(BinaryList data) {
            UnsafeBuffer buffer = new UnsafeBuffer();
            data.iterator().next().wrapValue(buffer);
            BinaryProcessMetadataDecoder decoder = new BinaryProcessMetadataDecoder();
            decoder.wrap(buffer, 0, BinaryProcessMetadataDecoder.BLOCK_LENGTH, 0);
            ProcessMetadata processMetadata = ProcessMetadata.deserialize(decoder);
            processMetadataFuture.complete(processMetadata);
            job.onProcessMetadata(processMetadata);
        }

        protected void onRecordingMetadata(BinaryList data) {
            UnsafeBuffer buffer = new UnsafeBuffer();
            data.iterator().next().wrapValue(buffer);
            BinaryRecordingMetadataDecoder decoder = new BinaryRecordingMetadataDecoder();
            decoder.wrap(buffer, 0, BinaryRecordingMetadataDecoder.BLOCK_LENGTH, 0);
            RecordingMetadata metadata = RecordingMetadata.deserialize(decoder);
            RecordingState recordingState = recordings.computeIfAbsent(
                metadata.getId(),
                () -> new RecordingState(
                    metadata,
                    index,
                    recordedMethodCallDataReader,
                    methods,
                    types)
            );
            recordingState.update(metadata);
            job.onRecordingMetadata(metadata);
        }

        private void onTypes(BinaryList data) {
            new TypeList(data).forEach(type -> types.store(type.getId(), type));
            job.onTypes(new TypeList(data));
        }

        private void onMethods(BinaryList data) {
            new MethodList(data).forEach(type -> methods.store(type.getId(), type));
            job.onMethods(new MethodList(data));
        }

        private void onRecordedCalls(BinaryListWithAddress data) {
            RecordedMethodCallList calls = new RecordedMethodCallList(data.getBytes());
            job.onRecordedCalls(data.getAddress(), calls);
        }
    }

    private abstract class StorageIteratingTask implements Runnable, Closeable {

        private final BinaryListFileReader reader;

        private StorageIteratingTask(File file) throws IOException {
            this.reader = new BinaryListFileReader(file);
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {

                try {
                    BinaryListWithAddress data = this.reader.readWithAddress();

                    if (data == null) {
                        if (continueOnNoData()) {
                            continue;
                        } else {
                            onComplete();
                            return;
                        }
                    }

                    switch (data.getBytes().id()) {
                        case ProcessMetadata.WIRE_ID:
                            onProcessMetadata(data.getBytes());
                            break;
                        case RecordingMetadata.WIRE_ID:
                            onRecordingMetadata(data.getBytes());
                            break;
                        case TypeList.WIRE_ID:
                            onTypes(data.getBytes());
                            break;
                        case MethodList.WIRE_ID:
                            onMethods(data.getBytes());
                            break;
                        case RecordedMethodCallList.WIRE_ID:
                            onRecordedCalls(data);
                            break;
                        case RecordingCompleteMark.WIRE_ID:
                            onComplete();
                            return;
                        default:
                            throw new StorageException("Unknown binary data id " + data.getBytes().id());
                    }
                } catch (Exception err) {
                    onError(err);
                    return;
                }
            }
        }

        protected abstract boolean continueOnNoData();

        protected abstract void onComplete();

        protected abstract void onError(Exception error);

        @Override
        public void close() {
            try {
                this.reader.close();
            } catch (IOException e) {
                // TODO log only
                throw new RuntimeException(e);
            }
        }

        protected abstract void onProcessMetadata(BinaryList data);

        protected abstract void onRecordingMetadata(BinaryList data);

        private void onTypes(BinaryList data) {
            new TypeList(data).forEach(type -> types.store(type.getId(), type));
        }

        private void onMethods(BinaryList data) {
            new MethodList(data).forEach(type -> methods.store(type.getId(), type));
        }

        protected abstract void onRecordedCalls(BinaryListWithAddress data);
    }

    private class SearchTask extends StorageIteratingTask {

        private final SearchQuery query;
        private final SearchResultListener searchResultListener;

        private SearchTask(File file, SearchQuery query, SearchResultListener resultListener) throws IOException {
            super(file);
            this.query = query;
            this.searchResultListener = resultListener;
            resultListener.onStart();
        }

        @Override
        protected boolean continueOnNoData() {
            return false;
        }

        @Override
        protected void onComplete() {
            searchResultListener.onEnd();
        }

        @Override
        protected void onError(Exception error) {

        }

        @Override
        protected void onProcessMetadata(BinaryList data) {

        }

        @Override
        protected void onRecordingMetadata(BinaryList data) {

        }

        @Override
        protected void onRecordedCalls(BinaryListWithAddress data) {
            RecordedMethodCallList calls = new RecordedMethodCallList(data.getBytes());

            for (RecordedMethodCall recordedCall : calls) {
                if (recordedCall instanceof RecordedEnterMethodCall) {
                    RecordedEnterMethodCall enterMethodCall = (RecordedEnterMethodCall) recordedCall;
                    if (query.matches(enterMethodCall, types, methods)) {
                        searchResultListener.onMatch(enterMethodCall);
                    }
                } else {
                    RecordedExitMethodCall exitMethodCall = (RecordedExitMethodCall) recordedCall;
                    if (query.matches(exitMethodCall, types, methods)) {
                        searchResultListener.onMatch(exitMethodCall);
                    }
                }
            }
        }
    }

    private class StorageReaderTask extends StorageIteratingTask {

        private final Backoff backoff = new FixedDelayBackoff(Duration.ofMillis(100));
        private final ReaderSettings settings;

        private StorageReaderTask(File file, ReaderSettings settings) throws IOException {
            super(file);
            this.settings = settings;
        }

        @Override
        protected boolean continueOnNoData() {
            try {
                backoff.await();
            } catch (InterruptedException e) {
                throw new StorageException("Interrupted", e);
            }
            return true;
        }

        @Override
        protected void onComplete() {
            finishedReadingFuture.complete(true);
        }

        @Override
        protected void onError(Exception error) {
            finishedReadingFuture.completeExceptionally(error);
        }

        @Override
        protected void onProcessMetadata(BinaryList data) {
            UnsafeBuffer buffer = new UnsafeBuffer();
            data.iterator().next().wrapValue(buffer);
            BinaryProcessMetadataDecoder decoder = new BinaryProcessMetadataDecoder();
            decoder.wrap(buffer, 0, BinaryProcessMetadataDecoder.BLOCK_LENGTH, 0);
            processMetadataFuture.complete(ProcessMetadata.deserialize(decoder));
        }

        @Override
        protected void onRecordingMetadata(BinaryList data) {
            UnsafeBuffer buffer = new UnsafeBuffer();
            data.iterator().next().wrapValue(buffer);
            BinaryRecordingMetadataDecoder decoder = new BinaryRecordingMetadataDecoder();
            decoder.wrap(buffer, 0, BinaryRecordingMetadataDecoder.BLOCK_LENGTH, 0);
            RecordingMetadata metadata = RecordingMetadata.deserialize(decoder);
            RecordingState recordingState = recordings.computeIfAbsent(
                    metadata.getId(),
                    () -> new RecordingState(
                            metadata,
                            index,
                            recordedMethodCallDataReader,
                            methods,
                            types)
            );
            recordingState.update(metadata);
        }

        @Override
        protected void onRecordedCalls(BinaryListWithAddress data) {
            RecordedMethodCallList recordedMethodCalls = new RecordedMethodCallList(data.getBytes());
            if (recordedMethodCalls.isEmpty()) {
                return;
            }
            int recordingId = recordedMethodCalls.getRecordingId();
            RecordingState recording = recordings.get(recordingId);
            if (recording == null) {
                return;
            }
            recording.onNewRecordedCalls(data.getAddress(), recordedMethodCalls);
            if (recording.isPublished()) {
                recordingListener.onRecordingUpdated(recording.toRecording());
            } else {
                Recording converted = recording.toRecording();
                if (recording.getRoot() != null && settings.getFilter().shouldPublish(converted) && recording.publish()) {
                    recordingListener.onRecordingUpdated(converted);
                }
            }
        }
    }
}
