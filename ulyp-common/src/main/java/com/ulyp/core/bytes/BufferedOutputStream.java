package com.ulyp.core.bytes;

import com.ulyp.core.util.SystemPropertyUtil;
import org.agrona.DirectBuffer;

import java.io.IOException;

public class BufferedOutputStream implements OutputStream {

    private static final int TMP_BUF_SIZE = SystemPropertyUtil.getInt("ulyp.storage.buffered-output-stream.size", 4 * 1024);

    private final java.io.OutputStream sink;
    private final byte[] buf;

    public BufferedOutputStream(java.io.OutputStream sink) {
        this.sink = sink;
        this.buf = new byte[TMP_BUF_SIZE];
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
