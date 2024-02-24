package com.ulyp.storage.util;

import java.io.*;

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
