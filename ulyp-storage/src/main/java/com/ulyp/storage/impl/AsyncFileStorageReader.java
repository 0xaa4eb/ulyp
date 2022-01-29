package com.ulyp.storage.impl;

import com.ulyp.core.repository.InMemoryRepository;
import com.ulyp.core.repository.Repository;
import com.ulyp.core.util.Backoff;
import com.ulyp.core.util.FixedDelayBackoff;
import com.ulyp.storage.RecordingListener;
import com.ulyp.storage.util.NamedThreadFactory;
import com.ulyp.core.*;
import com.ulyp.core.mem.BinaryList;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.storage.Recording;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.StorageReader;
import com.ulyp.transport.BinaryProcessMetadataDecoder;
import com.ulyp.transport.BinaryRecordingMetadataDecoder;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AsyncFileStorageReader implements StorageReader {

    private final File file;
    private final ExecutorService executorService;
    private final CompletableFuture<ProcessMetadata> processMetadata = new CompletableFuture<>();
    private final Repository<Long, Type> types = new InMemoryRepository<>();
    private final Repository<Integer, RecordingState> recordingStates = new InMemoryRepository<>();
    private final Repository<Long, Method> methods = new InMemoryRepository<>();
    private volatile RecordingListener recordingListener = RecordingListener.empty();

    public AsyncFileStorageReader(File file, boolean autoStart) {
        this.file = file;
        this.executorService = Executors.newFixedThreadPool(
                1,
                new NamedThreadFactory("Reader-" + file.toString(), true)
        );

        if (autoStart) {
            start();
        }
    }

    public synchronized void start() {
        try {
            Runnable task = new StorageReaderTask(file);
            this.executorService.submit(task);
        } catch (IOException e) {
            throw new StorageException("Could not start reader task for file " + file, e);
        }
    }

    private class StorageReaderTask implements Runnable, Closeable {

        private final BinaryListFileReader reader;

        private StorageReaderTask(File file) throws IOException {
            this.reader = new BinaryListFileReader(file);
        }

        @Override
        public void run() {
            Backoff backoff = new FixedDelayBackoff(Duration.ofMillis(100));

            while (!Thread.currentThread().isInterrupted()) {

                try {
                    BinaryListWithAddress data  = this.reader.readWithAddress();

                    if (data == null) {
                        backoff.await();
                        continue;
                    }

                    switch(data.getBytes().id()) {
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
                        default:
                            throw new StorageException("Unknown binary data id " + data.getBytes().id());
                    }
                } catch (Exception e) {

                    // TODO show in UI
                    e.printStackTrace();
                    return;
                }
            }
        }

        @Override
        public void close() throws IOException {

        }

        private void onProcessMetadata(BinaryList data) {
            UnsafeBuffer buffer = new UnsafeBuffer();
            data.iterator().next().wrapValue(buffer);
            BinaryProcessMetadataDecoder decoder = new BinaryProcessMetadataDecoder();
            decoder.wrap(buffer, 0, BinaryProcessMetadataDecoder.BLOCK_LENGTH, 0);
            processMetadata.complete(ProcessMetadata.deserialize(decoder));
        }

        private void onRecordingMetadata(BinaryList data) {
            UnsafeBuffer buffer = new UnsafeBuffer();
            data.iterator().next().wrapValue(buffer);
            BinaryRecordingMetadataDecoder decoder = new BinaryRecordingMetadataDecoder();
            decoder.wrap(buffer, 0, BinaryRecordingMetadataDecoder.BLOCK_LENGTH, 0);
            RecordingMetadata metadata = RecordingMetadata.deserialize(decoder);
            RecordingState recordingState = recordingStates.computeIfAbsent(
                    metadata.getId(),
                    () -> new RecordingState(
                            metadata,
                            new DataReader(file),
                            methods,
                            types,
                            recordingListener)
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
            RecordedMethodCall first = recordedMethodCalls.iterator().next();
            RecordingState recordingState = recordingStates.get(first.getRecordingId());
            recordingState.onRecordedCalls(data.getAddress(), recordedMethodCalls);
        }

        private void onMethods(BinaryList data) {
            new MethodList(data).forEach(type -> methods.store(type.getId(), type));
        }
    }

    public Repository<Long, Type> getTypes() {
        return types;
    }

    @Override
    public CompletableFuture<ProcessMetadata> getProcessMetadata() {
        return processMetadata;
    }

    @Override
    public void subscribe(RecordingListener listener) {
        this.recordingListener = listener;
    }

    @Override
    public List<Recording> availableRecordings() {
        return recordingStates.values()
                .stream()
                .map(Recording::new)
                .collect(Collectors.toList());
    }

    @Override
    public void close() throws StorageException {
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new StorageException("Interrupted", e);
        }
    }
}