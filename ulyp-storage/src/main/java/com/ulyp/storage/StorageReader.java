package com.ulyp.storage;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.storage.impl.EmptyStorageReader;

import java.util.List;

public interface StorageReader extends AutoCloseable {

    static StorageReader empty() {
        return new EmptyStorageReader();
    }

    ProcessMetadata getProcessMetadata();

    List<Recording> availableRecordings();

    void close() throws StorageException;
}
