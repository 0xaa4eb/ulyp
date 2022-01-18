package com.ulyp.storage.impl;

import com.ulyp.storage.StorageException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ByAddressFileReader {

    private final RandomAccessFile randomAccessFile;

    public ByAddressFileReader(File file) {
        try {
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
        randomAccessFile.readFully(buf);
        return buf;
    }

    public byte readByte(long address) throws IOException {
        randomAccessFile.seek(address);
        return randomAccessFile.readByte();
    }
}
