package com.ulyp.core.recorders.bytes;

import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.RecorderChooser;

import java.nio.charset.StandardCharsets;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

// writes to provided buffer
public class BufferBinaryOutput implements AutoCloseable, BinaryOutput {

    private static final int MAXIMUM_RECURSION_DEPTH = 3; // TODO configurable
    private static final int MAX_STRING_LENGTH = 200; // TODO configurable

    protected final MutableDirectBuffer buffer;
    protected int pos = 0;
    private int recursionDepth = 0;

    public BufferBinaryOutput(MutableDirectBuffer buffer) {
        this.buffer = buffer;
    }

    public void reset() {
        pos = 0;
        recursionDepth = 1;
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

    @Override
    public void write(DirectBuffer buffer) {
        write(buffer.capacity());
        this.buffer.putBytes(pos, buffer, 0, buffer.capacity());
        pos += buffer.capacity();
    }

    public void write(byte[] bytes) {
        write(bytes.length);
        buffer.putBytes(pos, bytes);
        pos += bytes.length;
    }

    public void write(String value) {
        if (value != null) {
            String toPrint;
            if (value.length() > MAX_STRING_LENGTH) {
                toPrint = value.substring(0, MAX_STRING_LENGTH) + "...(" + value.length() + ")";
            } else {
                toPrint = value;
            }

            byte[] bytes = toPrint.getBytes(StandardCharsets.UTF_8);
            write(bytes.length);
            for (byte b : bytes) {
                write(b);
            }
        } else {
            write(-1);
        }
    }

    public void write(Object object, TypeResolver typeResolver) throws Exception {
        try (BinaryOutput nestedOut = nest()) {
            Type itemType = typeResolver.get(object);
            write(itemType.getId());
            ObjectRecorder recorder;
            if (object != null) {
                // Simply stop recursively write objects if it's too deep
                recorder = recursionDepth() <= MAXIMUM_RECURSION_DEPTH ? (itemType.getRecorderHint() != null ? itemType.getRecorderHint() : RecorderChooser.getInstance().chooseForType(object.getClass())) : ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance();
            } else {
                recorder = ObjectRecorderRegistry.NULL_RECORDER.getInstance();
            }
            write(recorder.getId());
            recorder.write(object, nestedOut, typeResolver);
        }
    }

    @Override
    public void close() {
        recursionDepth--;
    }

    public int recursionDepth() {
        return recursionDepth;
    }

    @Override
    public int size() {
        return pos;
    }

    @Override
    public BufferBinaryOutput nest() {
        recursionDepth++;
        return this;
    }

    @Override
    public Checkpoint checkpoint() {
        final int currentPos = this.pos;
        return () -> this.pos = currentPos;
    }
}
