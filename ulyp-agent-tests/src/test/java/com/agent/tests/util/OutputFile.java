package com.agent.tests.util;

import com.ulyp.core.util.TempFile;
import com.ulyp.storage.reader.FileRecordingDataReaderBuilder;
import com.ulyp.storage.reader.RecordingDataReader;

public class OutputFile {

    private final TempFile file;

    public OutputFile() {
        this.file = new TempFile();
    }

    public RecordingDataReader toReader() {
        return new FileRecordingDataReaderBuilder(file.toPath().toFile()).build();
    }

    public TempFile getFile() {
        return file;
    }

    @Override
    public String toString() {
        return file.toPath().toString();
    }
}
