package com.ulyp.ui;

import com.ulyp.transport.ProcessInfo;
import lombok.Value;

import java.io.File;

@Value
public class FileRecordingsTabName {

    File file;
    ProcessInfo processInfo;

    public FileRecordingsTabName(File file, ProcessInfo processInfo) {
        this.file = file;
        this.processInfo = processInfo;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", file.getAbsolutePath(), processInfo.getMainClassName());
    }
}
