package com.ulyp.core.recorders.bytes;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderType;
import com.ulyp.core.Type;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.charset.StandardCharsets;

public final class BinaryOutputAppender implements AutoCloseable, BinaryOutput {

    private static final int MAXIMUM_RECURSION_DEPTH = 3;
    private static final int MAX_STRING_LENGTH = 200;

    private final byte[] tmp = new byte[32 * 1024];
    private final UnsafeBuffer tmpBuffer = new UnsafeBuffer(tmp);

    private final AbstractBinaryOutput binaryOutput;
    private int bytePos = 0;
    private int recursionDepth = 0;

    public BinaryOutputAppender(AbstractBinaryOutput binaryOutput) {
        this.binaryOutput = binaryOutput;
    }

    public void reset() {
        bytePos = 0;
        recursionDepth = 1;
    }

    public void append(boolean value) {
        append(value ? 1 : 0);
    }

    public void append(int value) {
        tmpBuffer.putInt(bytePos, value);
        bytePos += Integer.BYTES;
    }

    public void append(long value) {
        tmpBuffer.putLong(bytePos, value);
        bytePos += Long.BYTES;
    }

    public void append(byte c) {
        tmpBuffer.putByte(bytePos, c);
        bytePos += Byte.BYTES;
    }

    public void append(String value) {
        if (value != null) {
            String toPrint;
            if (value.length() > MAX_STRING_LENGTH) {
                toPrint = value.substring(0, MAX_STRING_LENGTH) + "...(" + value.length() + ")";
            } else {
                toPrint = value;
            }

            // TODO optimize for ASCII only strings
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
        Type itemType = typeResolver.get(object);
        append(itemType.getId());
        ObjectRecorder recorder;
        if (object != null) {
            // Simply stop recursively write objects if it's too deep
            recorder = recursionDepth() < MAXIMUM_RECURSION_DEPTH ? itemType.getSuggestedRecorder() : ObjectRecorderType.IDENTITY_RECORDER.getInstance();
        } else {
            recorder = ObjectRecorderType.NULL_RECORDER.getInstance();
        }
        append(recorder.getId());
        recorder.write(object, itemType, this, typeResolver);
    }

    @Override
    public void close() throws Exception {
        recursionDepth--;
        if (recursionDepth == 0) {
            binaryOutput.write(tmpBuffer, bytePos);
        }
    }

    public int recursionDepth() {
        return recursionDepth;
    }

    @Override
    public BinaryOutputAppender appender() {
        recursionDepth++;
        return this;
    }

    @Override
    public Checkpoint checkpoint() {
        final int currentPos = this.bytePos;
        return () -> this.bytePos = currentPos;
    }

    @Override
    public void writeBool(boolean val) throws Exception {
        append(val);
    }

    @Override
    public void writeInt(int val) throws Exception {
        append(val);
    }

    @Override
    public void writeLong(long val) throws Exception {
        append(val);
    }

    @Override
    public void writeString(String value) throws Exception {
        append(value);
    }
}
