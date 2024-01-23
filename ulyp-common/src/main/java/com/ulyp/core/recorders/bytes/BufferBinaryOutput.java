package com.ulyp.core.recorders.bytes;

import java.io.IOException;
import java.io.OutputStream;

import org.agrona.MutableDirectBuffer;

// writes to provided buffer
public class BufferBinaryOutput extends AbstractBinaryOutput {

    protected final MutableDirectBuffer buffer;

    public BufferBinaryOutput(MutableDirectBuffer buffer) {
        this.buffer = buffer;
    }

    public void write(boolean value) {
        write(value ? 1 : 0);
    }

    public void write(int value) {
        buffer.putInt(pos, value);
        pos += Integer.BYTES;
    }

    public void write(long value) {
        buffer.putLong(pos, value);
        pos += Long.BYTES;
    }

    public void write(byte c) {
        buffer.putByte(pos, c);
        pos += Byte.BYTES;
    }

    @Override
    public void write(char val) {
        buffer.putChar(pos, val);
        pos += Character.BYTES;
    }

    public void write(byte[] bytes) {
        write(bytes.length);
        buffer.putBytes(pos, bytes);
        pos += bytes.length;
    }

    @Override
    public int writeTo(OutputStream outputStream) throws IOException {
        for (int i = 0; i < pos; i++) {
            outputStream.write(buffer.getByte(i));
        }
        return pos;
    }

    @Override
    public void writeAt(int offset, int value) {
        buffer.putInt(offset, value);
    }

    @Override
    public void dispose() {

    }
}
