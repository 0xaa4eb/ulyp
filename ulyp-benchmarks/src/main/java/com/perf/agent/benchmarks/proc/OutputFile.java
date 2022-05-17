package com.perf.agent.benchmarks.proc;

import com.ulyp.core.exception.UlypException;
import com.ulyp.storage.StorageReader;
import com.ulyp.storage.impl.AsyncFileStorageReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        StorageReader reader = new AsyncFileStorageReader(file.toFile(), true);
        try {
            reader.getFinishedReadingFuture().get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new UlypException("Timed out waiting for recording to finish", e);
        }
        return reader;
    }

    @Override
    public String toString() {
        return "" + file;
    }
}
