package com.perf.agent.benchmarks.proc;

import com.ulyp.core.exception.UlypException;
import com.ulyp.storage.ReaderSettings;
import com.ulyp.storage.RecordingDataReader;
import com.ulyp.storage.impl.AsyncFileRecordingDataReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

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

    public RecordingDataReader toReader() throws InterruptedException {
        RecordingDataReader reader = new AsyncFileRecordingDataReader(ReaderSettings.builder().file(file.toFile()).autoStartReading(true).build());
        try {
            reader.getFinishedReadingFuture().get(180, TimeUnit.SECONDS);
        } catch (InterruptedException interruptedException) {
            throw interruptedException;
        } catch (Exception e) {
            throw new UlypException("Timed out waiting for recording to finish", e);
        }
        return reader;
    }

    public long size() {
        return file.toFile().length();
    }

    @Override
    public String toString() {
        return file.toString();
    }
}
