package com.ulyp.storage.impl;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.process.ProcessInfo;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.StorageWriter;

public class DevNullStorageWriter implements StorageWriter {

    @Override
    public void write(ProcessMetadata processInfo) throws StorageException {

    }

    @Override
    public void write(RecordingMetadata recordingMetadata) throws StorageException {

    }

    @Override
    public void write(TypeList types) throws StorageException {

    }

    @Override
    public void write(RecordedMethodCallList callRecords) throws StorageException {

    }

    @Override
    public void write(MethodList methods) throws StorageException {

    }

    @Override
    public void close() throws StorageException {

    }
}
