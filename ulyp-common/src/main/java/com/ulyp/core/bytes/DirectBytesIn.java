package com.ulyp.core.bytes;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.charset.StandardCharsets;

/**
 * Allows to read from direct buffer
 */
public class DirectBytesIn implements BytesIn {

    private final DirectBuffer buffer;
    private int pos = 0;

    public DirectBytesIn(DirectBuffer buffer) {
        this.buffer = buffer;
    }

    public DirectBytesIn(byte[] value) {
        this.buffer = new UnsafeBuffer(value);
    }

    @Override
    public int available() {
        return buffer.capacity();
    }

    @Override
    public boolean readBoolean() {
        byte val = readByte();
        return val == (byte) 1;
    }

    @Override
    public byte readByte() {
        byte val = buffer.getByte(pos);
        pos += Byte.BYTES;
        return val;
    }

    @Override
    public int readInt() {
        int val = buffer.getInt(pos);
        pos += Integer.BYTES;
        return val;
    }

    public int readVarInt() {
        byte tmp;
        if ((tmp = readByte()) >= 0) {
            return tmp;
        }
        int result = tmp & 0x7f;
        if ((tmp = readByte()) >= 0) {
            result |= tmp << 7;
        } else {
            result |= (tmp & 0x7f) << 7;
            if ((tmp = readByte()) >= 0) {
                result |= tmp << 14;
            } else {
                result |= (tmp & 0x7f) << 14;
                if ((tmp = readByte()) >= 0) {
                    result |= tmp << 21;
                } else {
                    result |= (tmp & 0x7f) << 21;
                    result |= (tmp = readByte()) << 28;
                    while (tmp < 0) {
                        // We get into this loop only in the case of overflow.
                        // By doing this, we can call getVarInt() instead of
                        // getVarLong() when we only need an int.
                        tmp = readByte();
                    }
                }
            }
        }
        return result;
    }

    @Override
    public int readInt(int offset) {
        return buffer.getInt(offset);
    }

    @Override
    public char readChar() {
        char val = buffer.getChar(pos);
        pos += Character.BYTES;
        return val;
    }

    @Override
    public long readLong() {
        long val = buffer.getLong(pos);
        pos += Long.BYTES;
        return val;
    }

    @Override
    public BytesIn readBytes() {
        int length = readVarInt();
        UnsafeBuffer newBuf = new UnsafeBuffer();
        newBuf.wrap(buffer, pos, length);
        pos += length;
        return new DirectBytesIn(newBuf);
    }

    @Override
    public BytesIn readBytes(int offset, int length) {
        UnsafeBuffer newBuf = new UnsafeBuffer();
        newBuf.wrap(buffer, offset, length);
        return new DirectBytesIn(newBuf);
    }

    @Override
    public ObjectRecord readObject(ByIdTypeResolver typeResolver) {
        Type itemClassType = typeResolver.getType(readVarInt());
        ObjectRecorder recorder = ObjectRecorderRegistry.recorderForId(readByte());
        return recorder.read(itemClassType, this, typeResolver);
    }

    @Override
    public int getPosition() {
        return pos;
    }

    @Override
    public void moveTo(int position) {
        this.pos = position;
    }

    @Override
    public int readIntAt(int offset) {
        return buffer.getInt(offset);
    }

    @Override
    public String readString() {
        int length = readVarInt();
        if (length >= 0) {
            byte[] buf = new byte[length];
            this.buffer.getBytes(pos, buf);
            pos += length;
            return new String(buf, StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }

    @Override
    public byte[] toByteArray() {
        byte[] result = new byte[buffer.capacity()];
        buffer.getBytes(0, result);
        return result;
    }
}
