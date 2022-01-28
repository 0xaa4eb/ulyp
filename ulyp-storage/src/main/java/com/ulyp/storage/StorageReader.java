package com.ulyp.storage;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.storage.impl.EmptyStorageReader;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface StorageReader extends AutoCloseable {

    static StorageReader empty() {
        return new EmptyStorageReader();
    }

    CompletableFuture<ProcessMetadata> getProcessMetadata();

    void subscribe(RecordingListener listener);

    List<Recording> availableRecordings();

    void close() throws StorageException;
}
