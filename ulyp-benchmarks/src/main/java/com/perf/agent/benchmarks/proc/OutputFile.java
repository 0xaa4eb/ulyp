package com.perf.agent.benchmarks.proc;

import com.ulyp.storage.StorageReader;
import com.ulyp.storage.impl.SameThreadFileStorageReader;

import java.io.*;
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

    public long byteSize() {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file.toFile(), "r")) {
            return randomAccessFile.length();
        } catch (Exception e) {
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
