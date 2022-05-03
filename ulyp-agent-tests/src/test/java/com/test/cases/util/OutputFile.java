package com.test.cases.util;

import com.ulyp.storage.StorageReader;
import com.ulyp.storage.impl.SameThreadFileStorageReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OutputFile {

    private final Path file;

    public OutputFile(String prefix, String suffix) {
        try {
            this.file = Files.createTempFile(prefix, suffix);
            file.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public StorageReader toReader() {
        return new SameThreadFileStorageReader(file.toFile());
    }

    @Override
    public String toString() {
        return "" + file;
    }
}
