package com.ulyp.core.bytes;

import com.ulyp.core.util.FixedSizeObjectPool;
import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.IOException;

/**
 * Allows to write to direct buffer
 */
public class BufferBytesOut extends AbstractBytesOut {

    private final FixedSizeObjectPool<MarkImpl> marksPool = new FixedSizeObjectPool<>(
            MarkImpl::new,
            3
    );

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
            marksPool.requite(this);
        }
    }

    @Override
    public Mark mark() {
        MarkImpl newMark = marksPool.borrow();
        newMark.markPos = this.position;
        return newMark;
    }

    @Override
    public int bytesWritten(int prevOffset) {
        return this.position - prevOffset;
    }

    public void write(boolean value) {
        byte byteValue = value ? (byte) 1 : (byte) 0;
        write(byteValue);
    }

    public void write(int value) {
        buffer.putInt(position, value);
        position += Integer.BYTES;
    }

    public void writeVarInt(int v) {
        do {
            int bits = v & 0x7F;
            v >>>= 7;
            byte b = (byte) (bits + ((v != 0) ? 0x80 : 0));
            write(b);
        } while (v != 0);
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
        writeVarInt(buffer.capacity());
        this.buffer.putBytes(position, buffer, 0, buffer.capacity());
        position += buffer.capacity();
    }

    public void write(byte[] bytes) {
        writeVarInt(bytes.length);
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
}
