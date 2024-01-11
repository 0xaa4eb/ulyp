package com.ulyp.core.recorders.bytes;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.charset.StandardCharsets;

public class BufferBinaryInput implements BinaryInput {

    private final DirectBuffer buffer;
    private int pos = 0;

    public BufferBinaryInput(DirectBuffer buffer) {
        this.buffer = buffer;
    }

    public BufferBinaryInput(byte[] value) {
        this.buffer = new UnsafeBuffer(value);
    }

    @Override
    public boolean readBoolean() {
        long val = readInt();
        return val == 1;
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
    public BinaryInput readBytes() {
        int length = readInt();
        UnsafeBuffer newBuf = new UnsafeBuffer();
        newBuf.wrap(buffer, pos, length);
        return new BufferBinaryInput(newBuf);
    }

    @Override
    public ObjectRecord readObject(ByIdTypeResolver typeResolver) {
        Type itemClassType = typeResolver.getType(readInt());
        ObjectRecorder recorder = ObjectRecorderRegistry.recorderForId(readByte());
        return recorder.read(itemClassType, this, typeResolver);
    }

    @Override
    public String readString() {
        int length = readInt();
        if (length >= 0) {
            byte[] buf = new byte[length];
            this.buffer.getBytes(pos, buf);
            pos += length;
            return new String(buf, StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }
}
