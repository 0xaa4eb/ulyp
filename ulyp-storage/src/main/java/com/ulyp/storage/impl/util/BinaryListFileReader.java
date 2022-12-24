package com.ulyp.storage.impl.util;

import com.google.common.base.Preconditions;
import com.ulyp.core.mem.BinaryList;
import com.ulyp.storage.impl.BinaryListWithAddress;

import java.io.*;


public class BinaryListFileReader implements AutoCloseable {

    private final RandomAccessFile randomAccessFile;
    private long address = 0;

    public BinaryListFileReader(File file) throws IOException {
        this.randomAccessFile = new RandomAccessFile(file, "r");
    }

    public BinaryListWithAddress readWithAddress() throws IOException {
        long desired = address + 1 + BinaryList.HEADER_LENGTH;
        if (randomAccessFile.length() < desired) {
            return null;
        }

        byte[] buf = new byte[1 + BinaryList.HEADER_LENGTH];

        randomAccessFile.seek(address);
        randomAccessFile.read(buf);

        if (buf[0] == 0) {
            return null;
        }

        BinaryList binaryList = new BinaryList(buf, 1);
        long length = binaryList.byteLength();
        int bytesToRead = (int) (length + 1);
        long binaryListAddress = address;
        randomAccessFile.seek(binaryListAddress);
        byte[] data = new byte[bytesToRead];
        int bytesRead = randomAccessFile.read(data);
        Preconditions.checkState(
                bytesRead == bytesToRead,
                "Binary list marked as fully written, but reader was not able to read " + bytesToRead +
                        " bytes. Read " + bytesRead + " bytes");
        BinaryList result = new BinaryList(data, 1);
        address += bytesRead;
        return BinaryListWithAddress.builder()
                .address(binaryListAddress + 1)
                .bytes(result)
                .build();
    }

    public BinaryList read() throws IOException {
        BinaryListWithAddress data = readWithAddress();
        return data != null ? data.getBytes() : null;
    }

    public void close() throws IOException {
        randomAccessFile.close();
    }
}
