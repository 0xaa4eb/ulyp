package com.ulyp.storage;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordedEnterMethodCall;
import com.ulyp.core.RecordedExitMethodCall;
import com.ulyp.storage.search.SearchQuery;
import com.ulyp.storage.search.SearchResultListener;
import com.ulyp.storage.tree.Recording;

import java.util.concurrent.CompletableFuture;

/**
 * The entry point to all data recorded by the agent. All reading happens in a background thread
 * asynchronously.
 * <p>
 * All recorded calls are aggregated to {@link Recording} instances.
 */
public interface RecordingDataReader extends AutoCloseable {

    void start();

    CompletableFuture<Void> submitReaderJob(RecordingDataReaderJob job);

    /**
     *
     */
    RecordedEnterMethodCall readEnterMethodCall(long address);

    /**
     *
     */
    RecordedExitMethodCall readExitMethodCall(long address);

    ProcessMetadata getProcessMetadata();

    void close() throws StorageException;

    CompletableFuture<Void> initiateSearch(SearchQuery query, SearchResultListener listener);
}
