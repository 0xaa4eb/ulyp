package com.ulyp.storage.impl;

import com.ulyp.core.mem.BinaryList;

import java.io.*;

public class BinaryListFileWriter implements AutoCloseable {

    private final ByAddressFileWriter byAddressFileWriter;
    private final OutputStream outputStream;
    private long address = 0;

    public BinaryListFileWriter(File file) throws IOException {
        this.outputStream = new BufferedOutputStream(new FileOutputStream(file, false));
        this.byAddressFileWriter = new ByAddressFileWriter(file);
    }

    public void append(BinaryList values) throws IOException {
        long lastAddr = address;
        outputStream.write(0);
        values.writeTo(this.outputStream);
        address += (values.byteLength() + 1);
        outputStream.flush();
        byAddressFileWriter.writeAt(lastAddr, (byte) 1);
    }

    public void close() throws IOException {
        try {
            outputStream.close();
        } finally {
            byAddressFileWriter.close();
        }
    }
}
