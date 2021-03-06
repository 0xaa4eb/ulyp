package com.ulyp.core.impl;

import java.io.*;

public class IndexedLog implements AutoCloseable {

    private final OutputStream outputStream;
    private final RandomAccessFile randomAccessFile;
    private long pos = 0;

    public IndexedLog(File file) {
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file, false));
            this.randomAccessFile = new RandomAccessFile(file, "r");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int readAt(long address, byte[] dst) {
        try {
            randomAccessFile.seek(address);
            return randomAccessFile.read(dst);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long pos() {
        return pos;
    }

    public void write(byte[] bytes) {
        try {
            outputStream.write(bytes);
            outputStream.flush();
            pos += bytes.length;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {

    }
}
