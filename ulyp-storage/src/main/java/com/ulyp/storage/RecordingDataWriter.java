package com.ulyp.storage;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.storage.impl.AsyncFileRecordingDataWriter;
import com.ulyp.storage.impl.DevNullRecordingDataWriter;
import com.ulyp.storage.impl.FileRecordingDataWriter;
import com.ulyp.storage.impl.StatsRecordingDataWriter;

import java.io.File;

public interface RecordingDataWriter extends AutoCloseable {

    static RecordingDataWriter statsRecording(RecordingDataWriter delegate) {
        return new StatsRecordingDataWriter(delegate);
    }

    static RecordingDataWriter async(RecordingDataWriter delegate) {
        return new AsyncFileRecordingDataWriter(delegate);
    }

    static RecordingDataWriter forFile(File file) {
        return new FileRecordingDataWriter(file);
    }

    static RecordingDataWriter devNull() {
        return new DevNullRecordingDataWriter();
    }

    void reset(ResetMetadata resetMetadata) throws StorageException;

    void write(ProcessMetadata processMetadata) throws StorageException;

    void write(RecordingMetadata recordingMetadata) throws StorageException;

    void write(TypeList types) throws StorageException;

    void write(MethodList methods) throws StorageException;

    void write(RecordedMethodCallList callRecords) throws StorageException;

    void close() throws StorageException;
}
