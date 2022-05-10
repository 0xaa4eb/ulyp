package com.agent.tests.util;

import com.ulyp.core.util.TempFile;
import com.ulyp.storage.StorageReader;
import com.ulyp.storage.impl.AsyncFileStorageReader;

public class OutputFile {

    private final TempFile file;

    public OutputFile() {
        this.file = new TempFile();
    }

    public StorageReader toReader() {
        return new AsyncFileStorageReader(file.toPath().toFile(), true);
    }

    @Override
    public String toString() {
        return file.toPath().toString();
    }
}
