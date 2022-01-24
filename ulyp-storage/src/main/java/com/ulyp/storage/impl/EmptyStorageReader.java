package com.ulyp.storage.impl;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.storage.Recording;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.StorageReader;

import java.util.Collections;
import java.util.List;

public class EmptyStorageReader implements StorageReader {

    @Override
    public ProcessMetadata getProcessMetadata() {
        return null;
    }

    @Override
    public List<Recording> availableRecordings() {
        return Collections.emptyList();
    }

    @Override
    public void close() throws StorageException {

    }
}
