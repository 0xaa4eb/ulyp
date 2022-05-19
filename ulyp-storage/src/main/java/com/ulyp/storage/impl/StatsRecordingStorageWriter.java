package com.ulyp.storage.impl;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.StorageWriter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class StatsRecordingStorageWriter implements StorageWriter {

    private final StorageWriter delegate;

    private final PerTypeStats typeStats = new PerTypeStats("Type");
    private final PerTypeStats methodStats = new PerTypeStats("Method");
    private final PerTypeStats callStats = new PerTypeStats("Recorded call");

    @Override
    public void write(ProcessMetadata processMetadata) throws StorageException {
        delegate.write(processMetadata);
    }

    @Override
    public void write(RecordingMetadata recordingMetadata) throws StorageException {
        delegate.write(recordingMetadata);
    }

    @Override
    public void write(TypeList types) throws StorageException {
        delegate.write(types);
        typeStats.addToCount(types.size());
        typeStats.addBytes(types.byteLength());
    }

    @Override
    public void write(RecordedMethodCallList callRecords) throws StorageException {
        delegate.write(callRecords);
        // Every call is recorded twice: as enter method call and exit method calls, therefore the value needs to be adjusted
        callStats.addToCount(callRecords.size() / 2);
        callStats.addBytes(callRecords.byteLength());
    }

    @Override
    public void write(MethodList methods) throws StorageException {
        delegate.write(methods);
        methodStats.addToCount(methods.size());
        methodStats.addBytes(methods.byteLength());
    }

    public List<PerTypeStats> getAllPerTypeStats() {
        return Arrays.asList(typeStats, methodStats, callStats);
    }

    @Override
    public void close() throws StorageException {
        System.out.println(typeStats);
        System.out.println(methodStats);
        System.out.println(callStats);
        delegate.close();
    }
}
