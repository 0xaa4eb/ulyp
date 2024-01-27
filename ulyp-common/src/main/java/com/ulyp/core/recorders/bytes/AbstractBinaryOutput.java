package com.ulyp.core.recorders.bytes;

import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.RecorderChooser;

import java.nio.charset.StandardCharsets;

public abstract class AbstractBinaryOutput implements AutoCloseable, BinaryOutput {

    private static final int MAXIMUM_RECURSION_DEPTH = 3; // TODO configurable
    private static final int MAX_STRING_LENGTH = 200; // TODO configurable

    protected int pos = 0;
    private int recursionDepth = 0;

    @Override
    public Checkpoint checkpoint() {
        final int currentPos = this.pos;
        return () -> this.pos = currentPos;
    }

    @Override
    public int currentOffset() {
        return this.pos;
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

    public int recursionDepth() {
        return recursionDepth;
    }

    @Override
    public BinaryOutput nest() {
        recursionDepth++;
        return this;
    }

    @Override
    public void close() {
        recursionDepth--;
    }
}
