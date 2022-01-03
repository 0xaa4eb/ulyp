package com.ulyp.storage.impl;

import com.ulyp.core.mem.BinaryList;
import com.ulyp.database.util.ByteUtils;

import java.io.*;

public class WithAddressOutputStream extends OutputStream {

    private final OutputStream delegate;
    private final ByAddressFileWriter byAddressFileWriter;
    private long currentAddress;

    public WithAddressOutputStream(File file) throws IOException {
        this.delegate = new BufferedOutputStream(new FileOutputStream(file, false));
        this.byAddressFileWriter = new ByAddressFileWriter(file);
        this.currentAddress = 0;
    }

    public long getSize() {
        return currentAddress;
    }

    public void writeLong(long value) throws IOException {
        this.delegate.write(ByteUtils.longToBytes(value));
        currentAddress += Long.BYTES;
    }

    public void writeBinaryList(BinaryList bytes, long id) throws IOException {
        writeLong(FlatFileConstants.BINARY_LIST_MAGIC);
        writeLong(id);
        long lengthAddress = currentAddress;
        writeLong(-1L);
        bytes.writeTo(this);
        byAddressFileWriter.writeAt(lengthAddress, bytes.length());
    }

    @Override
    public void write(int b) throws IOException {
        this.delegate.write(b);
        currentAddress++;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
