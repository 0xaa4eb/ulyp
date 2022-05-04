package com.agent.tests.util;

import com.ulyp.core.util.TempFile;
import com.ulyp.storage.StorageReader;
import com.ulyp.storage.impl.SameThreadFileStorageReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OutputFile {

    private final TempFile file;

    public OutputFile() {
        this.file = new TempFile();
    }

    public StorageReader toReader() {
        return new SameThreadFileStorageReader(file.toPath().toFile());
    }

    @Override
    public String toString() {
        return file.toPath().toString();
    }
}
