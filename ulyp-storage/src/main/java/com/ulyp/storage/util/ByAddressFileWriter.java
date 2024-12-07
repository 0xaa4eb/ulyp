package com.ulyp.storage.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ByAddressFileWriter implements AutoCloseable {

    private final RandomAccessFile randomAccessFile;

    public ByAddressFileWriter(File file) throws IOException {
        this.randomAccessFile = new RandomAccessFile(file, "rw");
    }

    public void writeAt(long addr, byte value) throws IOException {
        randomAccessFile.seek(addr);
        randomAccessFile.write(value);
    }

    public void close() throws IOException {
        randomAccessFile.close();
    }
}
