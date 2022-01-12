package com.ulyp.storage;

import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.process.ProcessInfo;

import java.io.IOException;

public interface StorageWriter extends AutoCloseable {

    // TODO move to sbe
    void store(ProcessInfo processInfo) throws StorageException;

    void store(RecordingMetadata recordingMetadata) throws StorageException;

    void store(TypeList types) throws StorageException;

    void store(RecordedMethodCallList callRecords) throws StorageException;

    void store(MethodList methods) throws StorageException;

    void close() throws StorageException;
}
