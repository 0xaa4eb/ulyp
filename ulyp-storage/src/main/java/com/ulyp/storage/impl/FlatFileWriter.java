package com.ulyp.storage.impl;

import org.agrona.concurrent.UnsafeBuffer;
import com.ulyp.core.mem.BinaryList;

import java.io.*;

public class FlatFileWriter implements AutoCloseable {

    private final OutputStream outputStream;
    private final RandomAccessFile randomAccessFile;

    public FlatFileWriter(File file) throws IOException {
        this.outputStream = new BufferedOutputStream(new FileOutputStream(file, false));
        this.randomAccessFile = new RandomAccessFile(file, "rw");
    }

    public void writeAt(long addr, long value) throws IOException {
        randomAccessFile.seek(addr);
        randomAccessFile.writeLong(value);
    }

    public void append(UnsafeBuffer buffer) throws IOException {
        for (int i = 0; i < buffer.capacity(); i++) {
            outputStream.write(buffer.getByte(i));
        }
    }

    public void append(BinaryList values) throws IOException {
        values.writeTo(this.outputStream);
    }

    public void close() throws IOException {
        try {
            outputStream.close();
        } finally {
            randomAccessFile.close();
        }
    }
}
