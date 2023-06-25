package com.agent.tests.util;

import com.ulyp.core.util.TempFile;
import com.ulyp.storage.ReaderSettings;
import com.ulyp.storage.RecordingDataReader;
import com.ulyp.storage.impl.AsyncFileRecordingDataReader;

public class OutputFile {

    private final TempFile file;

    public OutputFile() {
        this.file = new TempFile();
    }

    public RecordingDataReader toReader() {
        return new AsyncFileRecordingDataReader(ReaderSettings.builder().file(file.toPath().toFile()).autoStartReading(true).build());
    }

    @Override
    public String toString() {
        return file.toPath().toString();
    }
}
