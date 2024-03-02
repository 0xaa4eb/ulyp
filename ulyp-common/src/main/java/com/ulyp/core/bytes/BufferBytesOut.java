package com.ulyp.core.bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

/**
 * Allows to write to direct buffer
 */
public class BufferBytesOut extends AbstractBytesOut {

    private final List<MarkImpl> unusedMarks = new ArrayList<>();

    protected final MutableDirectBuffer buffer;

    public BufferBytesOut(MutableDirectBuffer buffer) {
        this.buffer = buffer;
    }

    private class MarkImpl implements Mark {

        private int markPos;

        @Override
        public int writtenBytes() {
            return BufferBytesOut.this.position - markPos;
        }

        @Override
        public void rollback() {
            BufferBytesOut.this.position = markPos;
        }

        @Override
        public void close() throws RuntimeException {
            // return to pool
            if (unusedMarks.size() < 3) {
                unusedMarks.add(this);
            }
        }
    }

    @Override
    public Mark mark() {
        MarkImpl newMark;
        if (!unusedMarks.isEmpty()) {
            newMark = unusedMarks.remove(unusedMarks.size() - 1);
        } else {
            newMark = new MarkImpl();
        }
        newMark.markPos = this.position;
        return newMark;
    }

    @Override
    public int bytesWritten(int prevOffset) {
        return this.position - prevOffset;
    }

    public void write(boolean value) {
        write(value ? 1 : 0);
    }

    public void write(int value) {
        buffer.putInt(position, value);
        position += Integer.BYTES;
    }

    public void write(long value) {
        buffer.putLong(position, value);
        position += Long.BYTES;
    }

    public void write(byte c) {
        buffer.putByte(position, c);
        position += Byte.BYTES;
    }

    @Override
    public void write(char val) {
        buffer.putChar(position, val);
        position += Character.BYTES;
    }

    @Override
    public DirectBuffer copy() {
        byte[] byteArray = new byte[position];
        this.buffer.getBytes(0, byteArray);
        return new UnsafeBuffer(byteArray);
    }

    @Override
    public void write(DirectBuffer buffer) {
        write(buffer.capacity());
        this.buffer.putBytes(position, buffer, 0, buffer.capacity());
        position += buffer.capacity();
    }

    public void write(byte[] bytes) {
        write(bytes.length);
        buffer.putBytes(position, bytes);
        position += bytes.length;
    }

    @Override
    public int writeTo(BytesOutputSink sink) throws IOException {
        sink.write(buffer, position);
        return position;
    }

    @Override
    public void writeAt(int offset, int value) {
        buffer.putInt(offset, value);
    }

    @Override
    public void dispose() {

    }
}
