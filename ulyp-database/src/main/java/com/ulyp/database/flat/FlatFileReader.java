package com.ulyp.database.flat;

import com.ulyp.core.mem.BinaryList;
import com.ulyp.database.DatabaseException;

import java.io.File;
import java.io.IOException;

public class FlatFileReader {

    private final ByAddressFileReader byAddressFileReader;
    private long currentAddress;

    public FlatFileReader(File file) throws IOException {
        this.byAddressFileReader = new ByAddressFileReader(file);
        this.currentAddress = 0;
    }

    public boolean hasMoreData() throws IOException {
        return byAddressFileReader.getLength() > currentAddress;
    }

    public boolean hasAtLeastBytes(long bytes) throws IOException {
        return byAddressFileReader.getLength() - currentAddress > bytes;
    }

    public long getCurrentAddress() {
        return currentAddress;
    }

    public BinaryListWithId readBinaryList() throws IOException, InterruptedException {
        long magic = readLong();
        if (magic != FlatFileConstants.BINARY_LIST_MAGIC) {
            throw new DatabaseException("Binary list magic is " + magic);
        }
        long id = readLong();
        long address = currentAddress;
        long bytesLength;
        while ((bytesLength = byAddressFileReader.readLongAt(address)) < 0) {
            Thread.sleep(50);
        }
        byte[] bytes = byAddressFileReader.readBytes(address + Long.BYTES, (int) bytesLength);
        currentAddress = address + Long.BYTES + bytesLength;
        BinaryList binaryList = new BinaryList(bytes);
        return BinaryListWithId.builder().binaryList(binaryList).id(id).build();
    }

    public long readLong() throws IOException {
        long value = byAddressFileReader.readLongAt(currentAddress);
        currentAddress += Long.BYTES;
        return value;
    }
}
