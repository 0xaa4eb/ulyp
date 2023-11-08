package com.ulyp.storage.reader;

import java.io.File;

public class FileRecordingDataReaderBuilder {

    private final File file;
    private int threads = Runtime.getRuntime().availableProcessors();

    public FileRecordingDataReaderBuilder(File file) {
        this.file = file;
    }

    public FileRecordingDataReader build() {
        return new FileRecordingDataReader(file, threads);
    }
}
