package com.ulyp.storage.util;

import com.ulyp.core.mem.WriteBinaryList;
import com.ulyp.core.util.BitUtil;
import com.ulyp.storage.StorageException;

import java.io.*;

public class BinaryListFileWriter implements AutoCloseable {

    private final File file;
    private ByAddressFileWriter byAddressFileWriter;
    private OutputStream outputStream;
    private long address = 0;

    public BinaryListFileWriter(File file) throws IOException {
        this.file = file;
        this.outputStream = new BufferedOutputStream(new FileOutputStream(file, false));
        this.byAddressFileWriter = new ByAddressFileWriter(file);
    }

    public void moveToBeginning() throws StorageException {
        try {
            this.byAddressFileWriter.close();
            this.outputStream.close();

            this.outputStream = new BufferedOutputStream(new FileOutputStream(file, false));
            this.byAddressFileWriter = new ByAddressFileWriter(file);
            this.address = 0;
        } catch (IOException e) {
            throw new StorageException("Could not move to beginning of file", e);
        }
    }

    public void append(WriteBinaryList values) throws StorageException {
        try {
            long lastAddr = address;
            outputStream.write(0);
            outputStream.write(1);
            outputStream.write(1);
            outputStream.write(1);
            outputStream.write(1);

            int bytesWritten = values.writeTo(this.outputStream);
            address += (bytesWritten + Byte.SIZE + Integer.SIZE);
            outputStream.flush();

            byAddressFileWriter.writeAt(lastAddr, (byte) 1);

            byAddressFileWriter.writeAt(lastAddr + 1, (byte) 1);

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
