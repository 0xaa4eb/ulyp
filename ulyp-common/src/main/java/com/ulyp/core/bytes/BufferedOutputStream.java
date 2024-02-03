package com.ulyp.core.bytes;

import org.agrona.DirectBuffer;

import java.io.IOException;

public class BufferedOutputStream implements OutputStream {

    private final java.io.OutputStream sink;
    private final byte[] buf;

    public BufferedOutputStream(java.io.OutputStream sink) {
        this.sink = sink;
        this.buf = new byte[1024 * 4]; // TODO configurable
    }

    @Override
    public void flush() throws IOException {
        sink.flush();
    }

    @Override
    public void write(byte b) throws IOException {
        sink.write(b);
    }

    @Override
    public void write(DirectBuffer buffer, int length) throws IOException {
        int offset = 0;
        while (offset < length) {
            int dataToWrite = Math.min(buf.length, (length - offset));
            buffer.getBytes(offset, buf, 0, dataToWrite); // TODO remove this copying
            sink.write(buf, 0, dataToWrite);
            offset += dataToWrite;
        }
    }

    @Override
    public void close() throws IOException {
        sink.close();
    }
}
