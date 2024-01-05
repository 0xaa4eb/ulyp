package com.ulyp.storage.writer;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.storage.StorageException;

public class BlackholeRecordingDataWriter implements RecordingDataWriter {

    @Override
    public void reset(ResetRequest resetRequest) throws StorageException {

    }

    @Override
    public void sync(Duration duration) {

    }

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
