package com.ulyp.core.bytes;

import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.RecorderChooser;
import com.ulyp.core.util.SystemPropertyUtil;

import java.nio.charset.StandardCharsets;

public abstract class AbstractBytesOut implements AutoCloseable, BytesOut {

    private static final int MAXIMUM_RECURSION_DEPTH = SystemPropertyUtil.getInt("ulyp.recorder.max-recursion", 3);
    private static final int MAX_STRING_LENGTH = SystemPropertyUtil.getInt("ulyp.recorder.max-string-length", 200);

    protected int position = 0;
    protected int recursionDepth = 0;

    @Override
    public int position() {
        return this.position;
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
            writeVarInt(bytes.length);
            for (byte b : bytes) {
                write(b);
            }
        } else {
            writeVarInt(-1);
        }
    }

    public void write(Object object, TypeResolver typeResolver) throws Exception {
        try (BytesOut nestedOut = nest()) {
            Type itemType = typeResolver.get(object);
            writeVarInt(itemType.getId());
            ObjectRecorder recorder;
            if (object != null) {
                // Simply stop recursively write objects if it's too deep
                if (recursionDepth() <= MAXIMUM_RECURSION_DEPTH) {
                    recorder = itemType.getRecorderHint();
                    if (recorder == null) {
                        recorder = RecorderChooser.getInstance().chooseForType(object.getClass());
                        itemType.setRecorderHint(recorder);
                    }
                } else {
                    recorder = ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance();
                }
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
    public BytesOut nest() {
        recursionDepth++;
        return this;
    }

    @Override
    public void close() {
        recursionDepth--;
    }
}
