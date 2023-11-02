package com.ulyp.storage.impl;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordedEnterMethodCall;
import com.ulyp.core.RecordedExitMethodCall;
import com.ulyp.storage.*;
import com.ulyp.storage.tree.Recording;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EmptyRecordingDataReader implements RecordingDataReader {

    @Override
    public void start() {

    }

    @Override
    public CompletableFuture<Void> submitJob(RecordingDataReaderJob job) {
        return null;
    }

    @Override
    public RecordedEnterMethodCall readEnterMethodCall(long address) {
        return null;
    }

    @Override
    public RecordedExitMethodCall readExitMethodCall(long address) {
        return null;
    }

    public CompletableFuture<ProcessMetadata> getProcessMetadataFuture() {
        return new CompletableFuture<>();
    }

    @Override
    public CompletableFuture<Boolean> getFinishedReadingFuture() {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public void subscribe(RecordingListener listener) {

    }

    @Override
    public List<Recording> getRecordings() {
        return Collections.emptyList();
    }

    @Override
    public void close() throws StorageException {

    }

    @Override
    public CompletableFuture<Void> initiateSearch(SearchQuery query, SearchResultListener listener) {
        return CompletableFuture.completedFuture(null);
    }
}
