/*
package com.ulyp.storage.writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.ulyp.core.*;
import com.ulyp.core.repository.InMemoryRepository;
import lombok.Getter;
import org.jetbrains.annotations.TestOnly;

import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.storage.StorageException;

@TestOnly
public class HeapRecordingDataWrtiter implements RecordingDataWriter {

    private final InMemoryRepository<Integer, Type> types = new InMemoryRepository<>();
    private final List<Method> methods = new ArrayList<>();
    @Getter
    private final List<RecordedMethodCall> callRecords = new ArrayList<>();

    @Override
    public void reset(ResetRequest resetRequest) throws StorageException {

    }

    @Override
    public void sync(Duration duration) {

    }

    @Override
    public void write(ProcessMetadata processMetadata) throws StorageException {

    }

    @Override
    public void write(RecordingMetadata recordingMetadata) throws StorageException {

    }

    @Override
    public void write(TypeList types) throws StorageException {
        types.forEach(type -> this.types.store(type.getId(), type));
    }

    @Override
    public void write(MethodList methods) throws StorageException {
        methods.forEach(this.methods::add);
    }

    @Override
    public void write(RecordedMethodCallList callRecords) throws StorageException {
        AddressableItemIterator<RecordedMethodCall> it = callRecords.iterator(types);
        while (it.hasNext()) {
            callRecords.add
        }
        callRecords.forEach(this.callRecords::add);
    }

    @Override
    public void close() throws StorageException {
        // NOP
    }
}*/
