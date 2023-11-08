package com.ulyp.storage.util;

import com.ulyp.storage.StorageException;

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

    public synchronized byte[] readBytes(long address, int bytesCount) throws IOException {
        randomAccessFile.seek(address);
        byte[] buf = new byte[bytesCount];
        randomAccessFile.read(buf);
        return buf;
    }

    @Override
    public String toString() {
        return "ByAddressFileReader{file=" + file + "}";
    }

    @Override
    public synchronized void close() throws IOException {
        randomAccessFile.close();
    }
}
