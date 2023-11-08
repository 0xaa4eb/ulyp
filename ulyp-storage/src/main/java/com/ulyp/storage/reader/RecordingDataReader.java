package com.ulyp.storage.reader;

import java.util.concurrent.CompletableFuture;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordedEnterMethodCall;
import com.ulyp.core.RecordedExitMethodCall;
import com.ulyp.storage.StorageException;

/**
 * The entry point to all data recorded by the agent
 */
public interface RecordingDataReader extends AutoCloseable {

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
}
