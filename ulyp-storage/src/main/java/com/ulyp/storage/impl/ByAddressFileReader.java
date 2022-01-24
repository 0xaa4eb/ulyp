package com.ulyp.storage.impl;

import com.ulyp.storage.StorageException;
import org.agrona.DirectBuffer;

import java.io.*;

public class ByAddressFileReader implements Closeable {

    private final File file;
    private final RandomAccessFile randomAccessFile;

    public ByAddressFileReader(File file) {
        try {
            this.file = file;
            this.randomAccessFile = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            throw new StorageException(e);
        }
    }

    public long getLength() throws IOException {
        return randomAccessFile.length();
    }

    public byte[] readBytes(long address, int bytesCount) throws IOException {
        randomAccessFile.seek(address);
        byte[] buf = new byte[bytesCount];
        randomAccessFile.read(buf);
        return buf;
    }

    public byte readByte(long address) throws IOException {
        randomAccessFile.seek(address);
        return randomAccessFile.readByte();
    }

    @Override
    public String toString() {
        return "ByAddressFileReader{file=" + file + "}";
    }

    @Override
    public void close() throws IOException {
        randomAccessFile.close();
    }
}
