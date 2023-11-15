package com.perf.agent.benchmarks.proc;

import com.ulyp.storage.reader.FileRecordingDataReader;
import com.ulyp.storage.reader.FileRecordingDataReaderBuilder;

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

    public RecordingResult toRecordingResult() {
        try (FileRecordingDataReader reader = new FileRecordingDataReaderBuilder(file.toFile()).build()) {
            return new RecordingResult(reader);
        }
    }

    public long size() {
        return file.toFile().length();
    }

    @Override
    public String toString() {
        return file.toString();
    }
}
