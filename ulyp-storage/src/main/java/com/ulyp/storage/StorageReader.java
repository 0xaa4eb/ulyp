package com.ulyp.storage;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.storage.impl.EmptyStorageReader;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The entry point to all data recorded by the agent. All reading happens in a background thread
 * asynchronously.
 *
 * All recorded calls are aggregated to {@link Recording} instances.
 */
public interface StorageReader extends AutoCloseable {

    static StorageReader empty() {
        return new EmptyStorageReader();
    }

    CompletableFuture<ProcessMetadata> getProcessMetadataFuture();

    /**
     * If reading finishes successfully, then the returned future will be completed
     * with true value, otherwise it completes exceptionally
     */
    CompletableFuture<Boolean> getFinishedReadingFuture();

    void subscribe(RecordingListener listener);

    List<Recording> availableRecordings();

    void close() throws StorageException;
}
