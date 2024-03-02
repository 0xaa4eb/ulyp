package com.ulyp.storage.util;

import com.ulyp.core.mem.OutputBytesList;
import com.ulyp.core.util.BitUtil;
import com.ulyp.storage.StorageException;

import java.io.*;

public class BinaryListFileWriter implements AutoCloseable {

    private final File file;
    private ByAddressFileWriter byAddressFileWriter;
    private com.ulyp.core.bytes.OutputStream outputStream;
    private long address = 0;

    public BinaryListFileWriter(File file) throws IOException {
        this.file = file;
        this.outputStream = new com.ulyp.core.bytes.BufferedOutputStream(new java.io.BufferedOutputStream(new FileOutputStream(file, false)));
        this.byAddressFileWriter = new ByAddressFileWriter(file);
    }

    public void moveToBeginning() throws StorageException {
        try {
            this.byAddressFileWriter.close();
            this.outputStream.close();

            this.outputStream = new com.ulyp.core.bytes.BufferedOutputStream(new FileOutputStream(file, false));
            this.byAddressFileWriter = new ByAddressFileWriter(file);
            this.address = 0;
        } catch (IOException e) {
            throw new StorageException("Could not move to beginning of file", e);
        }
    }

    public void write(OutputBytesList values) throws StorageException {
        try {
            long startAddr = address;
            outputStream.write((byte) 0);
            for (int i = 0; i < Integer.BYTES; i++) {
                outputStream.write((byte) 1);
            }

            int bytesWritten = values.writeTo(this.outputStream);
            address += (bytesWritten + Byte.BYTES + Integer.BYTES);
            outputStream.flush();

            byte[] bytesWrittenBytes = new byte[Integer.BYTES];
            BitUtil.intToBytes(bytesWritten, bytesWrittenBytes, 0);
            for (int i = 0; i < bytesWrittenBytes.length; i++) {
                byAddressFileWriter.writeAt(startAddr + Byte.BYTES + i, bytesWrittenBytes[i]);
            }

            byAddressFileWriter.writeAt(startAddr, (byte) 1);
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
