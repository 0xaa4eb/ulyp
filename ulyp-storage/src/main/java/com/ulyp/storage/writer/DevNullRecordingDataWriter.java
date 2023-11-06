package com.ulyp.storage.writer;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.storage.ResetMetadata;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.RecordingDataWriter;

public class DevNullRecordingDataWriter implements RecordingDataWriter {

    @Override
    public void reset(ResetMetadata resetMetadata) throws StorageException {

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
