package com.ulyp.storage.impl;

import com.ulyp.core.*;
import com.ulyp.core.mem.BinaryList;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.repository.InMemoryRepository;
import com.ulyp.core.repository.Repository;
import com.ulyp.storage.Recording;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.StorageReader;
import com.ulyp.transport.BinaryProcessMetadataDecoder;
import com.ulyp.transport.BinaryRecordingMetadataDecoder;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class SameThreadFileStorageReader implements StorageReader {

    private final File file;
    private ProcessMetadata processMetadata;
    private final Repository<Long, Type> types = new InMemoryRepository<>();
    private final Repository<Integer, RecordingState> recordingStates = new InMemoryRepository<>();
    private final Repository<Long, Method> methods = new InMemoryRepository<>();

    public SameThreadFileStorageReader(File file) {
        this.file = file;

        readFile(file);
    }

    private void readFile(File file) {
        try (BinaryListFileReader reader = new BinaryListFileReader(file)) {

            BinaryListWithAddress data;
            while ((data  = reader.readWithAddress(Duration.ofSeconds(1))) != null) {
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
            }
        } catch (IOException | InterruptedException e) {
            throw new StorageException(e);
        }
    }

    private void onProcessMetadata(BinaryList data) {
        UnsafeBuffer buffer = new UnsafeBuffer();
        data.iterator().next().wrapValue(buffer);
        BinaryProcessMetadataDecoder decoder = new BinaryProcessMetadataDecoder();
        decoder.wrap(buffer, 0, BinaryProcessMetadataDecoder.BLOCK_LENGTH, 0);
        processMetadata = ProcessMetadata.deserialize(decoder);
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

    @Override
    public ProcessMetadata getProcessMetadata() {
        return processMetadata;
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

    }
}
