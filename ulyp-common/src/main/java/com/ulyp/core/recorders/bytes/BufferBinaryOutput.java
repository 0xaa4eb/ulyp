package com.ulyp.core.recorders.bytes;

import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.RecorderChooser;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.ulyp.core.util.SystemPropertyUtil;
import org.agrona.MutableDirectBuffer;

// writes to provided buffer
public class BufferBinaryOutput implements AutoCloseable, BinaryOutput {

    private static final int MAXIMUM_RECURSION_DEPTH = SystemPropertyUtil.getInt("ulyp.recorder.max-recursion", 3);
    private static final int MAX_STRING_LENGTH = SystemPropertyUtil.getInt("ulyp.recorder.max-string-length", 200);

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

    @Override
    public int writeTo(OutputStream outputStream) throws IOException {
        for (int i = 0; i < pos; i++) {
            outputStream.write(buffer.getByte(i));
        }
        return pos;
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
    public int currentOffset() {
        return this.pos;
    }

    @Override
    public void writeAt(int offset, int value) {
        buffer.putInt(offset, value);
    }
}
