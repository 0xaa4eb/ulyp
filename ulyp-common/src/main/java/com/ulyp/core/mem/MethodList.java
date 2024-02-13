package com.ulyp.core.mem;

import com.ulyp.core.Method;
import com.ulyp.core.bytes.BufferBinaryOutput;
import com.ulyp.core.serializers.MethodSerializer;
import org.agrona.ExpandableDirectByteBuffer;

public class MethodList {

    public static final int WIRE_ID = 3;

    private final OutputBinaryList bytes;

    public MethodList() {
        bytes = new OutputBinaryList(WIRE_ID, new BufferBinaryOutput(new ExpandableDirectByteBuffer()));
    }

    public void add(Method method) {
        bytes.add(out -> MethodSerializer.instance.serialize(out, method));
    }

    public int size() {
        return bytes.size();
    }

    public OutputBinaryList getBytes() {
        return bytes;
    }

    public int byteLength() {
        return bytes.bytesWritten();
    }
}
