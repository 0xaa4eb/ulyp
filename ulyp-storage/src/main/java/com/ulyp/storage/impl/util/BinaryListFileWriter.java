package com.ulyp.storage.impl.util;

import com.ulyp.core.mem.BinaryList;
import com.ulyp.storage.StorageException;

import java.io.*;

public class BinaryListFileWriter implements AutoCloseable {

    private final ByAddressFileWriter byAddressFileWriter;
    private final OutputStream outputStream;
    private long address = 0;

    public BinaryListFileWriter(File file) throws IOException {
        this.outputStream = new BufferedOutputStream(new FileOutputStream(file, false));
        this.byAddressFileWriter = new ByAddressFileWriter(file);
    }

    public void append(BinaryList values) throws StorageException {
        try {
            long lastAddr = address;
            outputStream.write(0);
            values.writeTo(this.outputStream);
            address += (values.byteLength() + 1);
            outputStream.flush();
            byAddressFileWriter.writeAt(lastAddr, (byte) 1);
        } catch (IOException ioe) {
            throw new StorageException("Error while writing data", ioe);
        }
    }

    public void close() throws StorageException {
        try {
            try {
                outputStream.close();
            } finally {
                byAddressFileWriter.close();
            }
        } catch (IOException e) {
            throw new StorageException("Errr while closing writer", e);
        }
    }
}
