package com.ulyp.storage.writer;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.storage.StorageException;

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

    static RecordingDataWriter blackhole() {
        return new BlackholeRecordingDataWriter();
    }

    void write(ProcessMetadata processMetadata) throws StorageException;

    /**
     * Trims a recording file. The request should have all known type and method metadata.
     */
    void reset(ResetRequest resetRequest) throws StorageException;

    void write(RecordingMetadata recordingMetadata) throws StorageException;

    void write(TypeList types) throws StorageException;

    void write(MethodList methods) throws StorageException;

    void write(RecordedMethodCallList callRecords) throws StorageException;

    void close() throws StorageException;
}
