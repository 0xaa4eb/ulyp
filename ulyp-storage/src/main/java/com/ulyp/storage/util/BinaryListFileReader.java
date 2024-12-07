package com.ulyp.storage.util;

import com.ulyp.core.bytes.DirectBytesIn;
import com.ulyp.core.mem.InputBytesList;
import com.ulyp.core.util.BitUtil;
import com.ulyp.core.util.Preconditions;
import com.ulyp.storage.reader.BinaryListWithAddress;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


public class BinaryListFileReader implements AutoCloseable {

    public static final int HEADER_SIZE = Byte.BYTES + Integer.BYTES;

    private final RandomAccessFile randomAccessFile;
    private long address = 0;

    public BinaryListFileReader(File file) throws IOException {
        this.randomAccessFile = new RandomAccessFile(file, "r");
    }

    public BinaryListWithAddress readWithAddress() throws IOException {
        long desired = address + HEADER_SIZE;
        if (randomAccessFile.length() < desired) {
            return null;
        }

        byte[] buf = new byte[HEADER_SIZE];

        randomAccessFile.seek(address);
        randomAccessFile.read(buf);

        if (buf[0] == 0) {
            return null;
        }

        long length = BitUtil.bytesToInt(buf, 1);
        int bytesToRead = (int) (length + HEADER_SIZE);
        long address = this.address;
        randomAccessFile.seek(address);
        byte[] data = new byte[bytesToRead];
        int bytesRead = randomAccessFile.read(data);
        Preconditions.checkState(
                bytesRead == bytesToRead,
                "Binary list marked as fully written, but reader was not able to read " + bytesToRead +
                        " bytes. Read " + bytesRead + " bytes");
        UnsafeBuffer buffer = new UnsafeBuffer();
        buffer.wrap(data, HEADER_SIZE, bytesToRead - HEADER_SIZE);
        InputBytesList in = new InputBytesList(new DirectBytesIn(buffer));
        this.address += bytesRead;
        return BinaryListWithAddress.builder()
                .address(address + HEADER_SIZE)
                .bytes(in)
                .build();
    }

    public InputBytesList read() throws IOException {
        BinaryListWithAddress data = readWithAddress();
        return data != null ? data.getBytes() : null;
    }

    public void close() throws IOException {
        randomAccessFile.close();
    }
}
