package com.ulyp.storage.impl;

import com.ulyp.agent.transport.NamedThreadFactory;
import com.ulyp.core.Method;
import com.ulyp.core.RecordedMethodCall;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.Type;
import com.ulyp.core.mem.BinaryList;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.storage.Recording;
import com.ulyp.core.Repository;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.StorageReader;
import com.ulyp.transport.BinaryRecordingMetadataDecoder;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class StorageReaderImpl implements StorageReader {

    private final File file;
    private final ExecutorService executorService;

    private final Repository<Type> types = new InMemoryRepository<>();
    private final Repository<RecordingState> recordingStates = new InMemoryRepository<>();
    private final Repository<Method> methods = new InMemoryRepository<>();

    public StorageReaderImpl(File file) {
        this.file = file;
        this.executorService = Executors.newFixedThreadPool(
                1,
                new NamedThreadFactory("Reader-" + file.toString(), false)
        );

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
            while (!Thread.currentThread().isInterrupted()) {

                try {
                    BinaryListWithAddress data  = this.reader.readWithAddress(Duration.ofSeconds(1));

                    if (data == null) {
                        continue;
                    }

                    switch(data.getBytes().id()) {
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
            RecordedMethodCall first = recordedMethodCalls.iterator().next();
            RecordingState recordingState = recordingStates.get(first.getRecordingId());
            recordingState.onRecordedCalls(data.getAddress(), recordedMethodCalls);
        }

        private void onMethods(BinaryList data) {
            new MethodList(data).forEach(type -> methods.store(type.getId(), type));
        }
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
