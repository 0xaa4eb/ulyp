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
import com.ulyp.transport.BinaryProcessMetadataDecoder;
import com.ulyp.transport.BinaryRecordingMetadataDecoder;
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
    private final ReaderSettings settings;
    private final ExecutorService executorService;
    private final CompletableFuture<ProcessMetadata> processMetadataFuture = new CompletableFuture<>();
    private final CompletableFuture<Boolean> finishedReadingFuture = new CompletableFuture<>();
    private final Index index;
    private final InMemoryRepository<Integer, Type> types = new InMemoryRepository<>();
    private final InMemoryRepository<Integer, RecordingState> recordings = new InMemoryRepository<>();
    private final Repository<Integer, Method> methods = new InMemoryRepository<>();
    private volatile StorageReaderTask readingTask;
    private volatile RecordingListener recordingListener = RecordingListener.empty();

    public AsyncFileRecordingDataReader(ReaderSettings settings) {
        this.file = settings.getFile();
        this.index = settings.getIndexSupplier().get();
        this.settings = settings;
        this.executorService = Executors.newFixedThreadPool(
                1,
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

    public InMemoryRepository<Integer, Type> getTypes() {
        return types;
    }

    public CompletableFuture<ProcessMetadata> getProcessMetadataFuture() {
        return processMetadataFuture;
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

        recordings.values().forEach(state -> {
            try {
                state.close();
            } catch (IOException e) {
                // TODO log only
            }
        });
    }

    private class StorageReaderTask implements Runnable, Closeable {

        private final BinaryListFileReader reader;
        private final ReaderSettings settings;

        private StorageReaderTask(File file, ReaderSettings settings) throws IOException {
            this.reader = new BinaryListFileReader(file);
            this.settings = settings;
        }

        @Override
        public void run() {
            Backoff backoff = new FixedDelayBackoff(Duration.ofMillis(100));

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    BinaryListWithAddress data = this.reader.readWithAddress();

                    if (data == null) {
                        backoff.await();
                        continue;
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
                            ForkJoinPool.commonPool().execute(executorService::shutdownNow);
                            finishedReadingFuture.complete(true);
                            return;
                        default:
                            throw new StorageException("Unknown binary data id " + data.getBytes().id());
                    }
                } catch (InterruptedException ie) {
                    return;
                } catch (Exception err) {
                    finishedReadingFuture.completeExceptionally(err);
                    return;
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
            processMetadataFuture.complete(ProcessMetadata.deserialize(decoder));
        }

        private void onRecordingMetadata(BinaryList data) {
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
                            new DataReader(file),
                            methods,
                            types)
            );
            recordingState.update(metadata);
        }

        private void onTypes(BinaryList data) {
            new TypeList(data).forEach(type -> types.store(type.getId(), type));
        }

        private void onRecordedCalls(BinaryListWithAddress data) {
            RecordedMethodCallList recordedMethodCalls = new RecordedMethodCallList(data.getBytes());
            if (recordedMethodCalls.isEmpty()) {
                return;
            }
            int recordingId = recordedMethodCalls.iterator().next().getRecordingId();
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

        private void onMethods(BinaryList data) {
            new MethodList(data).forEach(type -> methods.store(type.getId(), type));
        }
    }
}
