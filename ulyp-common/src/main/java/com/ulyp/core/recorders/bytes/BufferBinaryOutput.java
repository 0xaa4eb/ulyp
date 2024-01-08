package com.ulyp.core.recorders.bytes;

import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.RecorderChooser;

import java.nio.charset.StandardCharsets;

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

    public void append(boolean value) {
        append(value ? 1 : 0);
    }

    public void append(int value) {
        buffer.putInt(pos, value);
        pos += Integer.BYTES;
    }

    public void append(long value) {
        buffer.putLong(pos, value);
        pos += Long.BYTES;
    }

    public void append(byte c) {
        buffer.putByte(pos, c);
        pos += Byte.BYTES;
    }

    public void append(byte[] bytes) {
        append(bytes.length);
        buffer.putBytes(pos, bytes);
        pos += bytes.length;
    }

    public void append(String value) {
        if (value != null) {
            String toPrint;
            if (value.length() > MAX_STRING_LENGTH) {
                toPrint = value.substring(0, MAX_STRING_LENGTH) + "...(" + value.length() + ")";
            } else {
                toPrint = value;
            }

            byte[] bytes = toPrint.getBytes(StandardCharsets.UTF_8);
            append(bytes.length);
            for (byte b : bytes) {
                append(b);
            }
        } else {
            append(-1);
        }
    }

    public void append(Object object, TypeResolver typeResolver) throws Exception {
        try (BinaryOutput nestedOut = nest()) {
            Type itemType = typeResolver.get(object);
            append(itemType.getId());
            ObjectRecorder recorder;
            if (object != null) {
                // Simply stop recursively write objects if it's too deep
                recorder = recursionDepth() <= MAXIMUM_RECURSION_DEPTH ? (itemType.getRecorderHint() != null ? itemType.getRecorderHint() : RecorderChooser.getInstance().chooseForType(object.getClass())) : ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance();
            } else {
                recorder = ObjectRecorderRegistry.NULL_RECORDER.getInstance();
            }
            append(recorder.getId());
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
    public BufferBinaryOutput nest() {
        recursionDepth++;
        return this;
    }

    @Override
    public Checkpoint checkpoint() {
        final int currentPos = this.pos;
        return () -> this.pos = currentPos;
    }

    @Override
    public void writeBool(boolean val) {
        append(val);
    }

    @Override
    public void writeChar(char val) {
        append(val);
    }

    @Override
    public void writeInt(int val) {
        append(val);
    }

    @Override
    public void writeLong(long val) {
        append(val);
    }

    @Override
    public void writeString(String value) {
        append(value);
    }

    @Override
    public void writeBytes(byte[] bytes) {
        append(bytes);
    }
}
