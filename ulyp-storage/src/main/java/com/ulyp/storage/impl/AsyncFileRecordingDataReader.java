package com.ulyp.storage.impl;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.agrona.concurrent.UnsafeBuffer;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordedEnterMethodCall;
import com.ulyp.core.RecordedExitMethodCall;
import com.ulyp.core.RecordingCompleteMark;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.BinaryList;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.util.NamedThreadFactory;
import com.ulyp.storage.ReaderSettings;
import com.ulyp.storage.RecordingDataReader;
import com.ulyp.storage.RecordingDataReaderJob;
import com.ulyp.storage.RecordingListener;
import com.ulyp.storage.SearchQuery;
import com.ulyp.storage.SearchResultListener;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.impl.util.BinaryListFileReader;
import com.ulyp.storage.tree.Index;
import com.ulyp.storage.tree.Recording;
import com.ulyp.transport.BinaryProcessMetadataDecoder;
import com.ulyp.transport.BinaryRecordingMetadataDecoder;

public class AsyncFileRecordingDataReader implements RecordingDataReader {

    private final File file;
    private final RecordedMethodCallDataReader recordedMethodCallDataReader;
    private final ReaderSettings settings;
    private final ExecutorService executorService;
    private final Index index;
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
    public CompletableFuture<ProcessMetadata> getProcessMetadataFuture() {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> getFinishedReadingFuture() {
        return null;
    }

    @Override
    public void subscribe(RecordingListener listener) {
        this.recordingListener = listener;
    }

    @Override
    public List<Recording> getRecordings() {
        return null;
    }

    @Override
    public void close() throws StorageException {
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // nop
        }
    }

    @Override
    public CompletableFuture<Void> initiateSearch(SearchQuery query, SearchResultListener listener) {
        /*try {
            return CompletableFuture.runAsync(new SearchTask(file, query, listener), executorService);
        } catch (IOException e) {
            throw new StorageException(e);
        }*/
        return null;
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
            job.onProcessMetadata(processMetadata);
        }

        protected void onRecordingMetadata(BinaryList data) {
            UnsafeBuffer buffer = new UnsafeBuffer();
            data.iterator().next().wrapValue(buffer);
            BinaryRecordingMetadataDecoder decoder = new BinaryRecordingMetadataDecoder();
            decoder.wrap(buffer, 0, BinaryRecordingMetadataDecoder.BLOCK_LENGTH, 0);
            RecordingMetadata metadata = RecordingMetadata.deserialize(decoder);
            job.onRecordingMetadata(metadata);
        }

        private void onTypes(BinaryList data) {
            job.onTypes(new TypeList(data));
        }

        private void onMethods(BinaryList data) {
            job.onMethods(new MethodList(data));
        }

        private void onRecordedCalls(BinaryListWithAddress data) {
            RecordedMethodCallList calls = new RecordedMethodCallList(data.getBytes());
            job.onRecordedCalls(data.getAddress(), calls);
        }
    }
}
