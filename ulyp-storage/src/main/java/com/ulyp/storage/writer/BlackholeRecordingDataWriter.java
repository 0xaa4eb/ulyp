package com.ulyp.storage.writer;

import java.time.Duration;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.SerializedMethodList;
import com.ulyp.core.mem.SerializedRecordedMethodCallList;
import com.ulyp.core.mem.SerializedTypeList;
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
    public void write(SerializedTypeList types) throws StorageException {

    }

    @Override
    public void write(SerializedRecordedMethodCallList callRecords) throws StorageException {

    }

    @Override
    public long estimateBytesWritten() {
        return 0;
    }

    @Override
    public void write(SerializedMethodList methods) throws StorageException {

    }

    @Override
    public void close() throws StorageException {

    }
}
