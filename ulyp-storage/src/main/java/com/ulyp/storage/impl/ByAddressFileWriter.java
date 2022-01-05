package com.ulyp.storage.impl;

import java.io.*;

public class ByAddressFileWriter implements AutoCloseable {

    private final OutputStream outputStream;
    private final RandomAccessFile randomAccessFile;

    public ByAddressFileWriter(File file) throws IOException {
        this.outputStream = new BufferedOutputStream(new FileOutputStream(file, false));
        this.randomAccessFile = new RandomAccessFile(file, "rw");
    }

    public void writeAt(long addr, byte value) throws IOException {
        randomAccessFile.seek(addr);
        randomAccessFile.write(value);
    }

    public void close() throws IOException {
        try {
            outputStream.close();
        } finally {
            randomAccessFile.close();
        }
    }
}
