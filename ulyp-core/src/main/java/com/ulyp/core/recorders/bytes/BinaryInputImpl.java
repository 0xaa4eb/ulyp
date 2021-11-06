package com.ulyp.core.recorders.bytes;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectBinaryPrinterType;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.Type;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.charset.StandardCharsets;

public class BinaryInputImpl implements BinaryInput {

    private final DirectBuffer buffer;
    private int bytePos = 0;

    public BinaryInputImpl(DirectBuffer buffer) {
        this.buffer = buffer;
    }

    public BinaryInputImpl(byte[] value) {
        this.buffer = new UnsafeBuffer(value);
    }

    @Override
    public boolean readBoolean() {
        long val = readInt();
        return val == 1;
    }

    @Override
    public byte readByte() {
        byte val = buffer.getByte(bytePos);
        bytePos += Byte.BYTES;
        return val;
    }

    @Override
    public int readInt() {
        int val = buffer.getInt(bytePos);
        bytePos += Integer.BYTES;
        return val;
    }

    @Override
    public long readLong() {
        long val = buffer.getLong(bytePos);
        bytePos += Long.BYTES;
        return val;
    }

    @Override
    public ObjectRecord readObject(ByIdTypeResolver typeResolver) {
        Type itemClassType = typeResolver.getType(readLong());
        ObjectRecorder printer = ObjectBinaryPrinterType.printerForId(readByte());
        return printer.read(itemClassType, this, typeResolver);
    }

    @Override
    public String readString() {
        int length = readInt();
        if (length >= 0) {
            byte[] buf = new byte[length];
            this.buffer.getBytes(bytePos, buf);
            bytePos += length;
            return new String(buf, StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }
}
