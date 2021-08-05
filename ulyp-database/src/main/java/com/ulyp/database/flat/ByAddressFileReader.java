package com.ulyp.database.flat;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ByAddressFileReader {

    private final RandomAccessFile randomAccessFile;

    public ByAddressFileReader(File file) throws IOException {
        this.randomAccessFile = new RandomAccessFile(file, "r");
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

    public long readLongAt(long address) throws IOException {
        randomAccessFile.seek(address);
        return randomAccessFile.readLong();
    }
}
