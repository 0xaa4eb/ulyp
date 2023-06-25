package com.ulyp.storage.util;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.TestOnly;

import com.ulyp.core.Method;
import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordedMethodCall;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.Type;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.storage.ResetMetadata;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.RecordingDataWriter;

@TestOnly
public class HeapRecordingDataWrtiter implements RecordingDataWriter {

    private final List<Type> types = new ArrayList<>();
    private final List<Method> methods = new ArrayList<>();
    private final List<RecordedMethodCall> callRecords = new ArrayList<>();

    @Override
    public void reset(ResetMetadata resetMetadata) throws StorageException {

    }

    @Override
    public void write(ProcessMetadata processMetadata) throws StorageException {

    }

    @Override
    public void write(RecordingMetadata recordingMetadata) throws StorageException {

    }

    @Override
    public void write(TypeList types) throws StorageException {
        types.forEach(this.types::add);
    }

    @Override
    public void write(MethodList methods) throws StorageException {
        methods.forEach(this.methods::add);
    }

    @Override
    public void write(RecordedMethodCallList callRecords) throws StorageException {
        callRecords.forEach(this.callRecords::add);
    }

    @Override
    public void close() throws StorageException {
        // NOP
    }

    public List<RecordedMethodCall> getCallRecords() {
        return callRecords;
    }
}
