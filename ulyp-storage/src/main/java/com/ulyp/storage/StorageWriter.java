package com.ulyp.storage;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.storage.impl.AsyncFileStorageWriter;
import com.ulyp.storage.impl.DevNullStorageWriter;
import com.ulyp.storage.impl.FileStorageWriter;
import com.ulyp.storage.impl.StatsRecordingStorageWriter;

import java.io.File;

public interface StorageWriter extends AutoCloseable {

    static StorageWriter statsRecording(StorageWriter delegate) {
        return new StatsRecordingStorageWriter(delegate);
    }

    static StorageWriter async(StorageWriter delegate) {
        return new AsyncFileStorageWriter(delegate);
    }

    static StorageWriter forFile(File file) {
        return new FileStorageWriter(file);
    }

    static StorageWriter devNull() {
        return new DevNullStorageWriter();
    }

    void write(ProcessMetadata processMetadata) throws StorageException;

    void write(RecordingMetadata recordingMetadata) throws StorageException;

    void write(TypeList types) throws StorageException;

    void write(RecordedMethodCallList callRecords) throws StorageException;

    void write(MethodList methods) throws StorageException;

    void close() throws StorageException;
}
