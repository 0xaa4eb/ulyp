package com.ulyp.storage.impl;

import com.google.common.base.Preconditions;
import com.ulyp.core.mem.BinaryList;

import java.io.*;
import java.time.Duration;


public class BinaryListFileReader implements AutoCloseable {

    private final InputStream inputStream;
    private final RandomAccessFile randomAccessFile;
    private long address = 0;

    public BinaryListFileReader(File file) throws IOException {
        this.inputStream = new BufferedInputStream(new FileInputStream(file));
        // TODO read only permission
        this.randomAccessFile = new RandomAccessFile(file, "rw");
    }

    public BinaryListWithAddress readWithAddress(Duration timeout) throws IOException, InterruptedException {
        long desired = address + 1 + BinaryList.HEADER_LENGTH;
        if (randomAccessFile.length() < desired) {
            return null;
        }

        byte[] buf = new byte[1 + BinaryList.HEADER_LENGTH];
        long deadline = System.currentTimeMillis() + timeout.toMillis();

        while (System.currentTimeMillis() < deadline) {
            randomAccessFile.seek(address);
            randomAccessFile.read(buf);

            if (buf[0] == 0) {
                // TODO make arg
                Thread.sleep(50);
                continue;
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

        return null;
    }

    public BinaryList read(Duration timeout) throws IOException, InterruptedException {
        BinaryListWithAddress data = readWithAddress(timeout);
        return data != null ? data.getBytes() : null;
    }

    public void close() throws IOException {
        try {
            inputStream.close();
        } finally {
            randomAccessFile.close();
        }
    }
}
