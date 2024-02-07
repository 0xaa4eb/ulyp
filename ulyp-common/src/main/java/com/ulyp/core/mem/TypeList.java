package com.ulyp.core.mem;

import com.ulyp.core.Type;
import com.ulyp.core.recorders.bytes.BufferBinaryOutput;
import com.ulyp.core.serializers.TypeSerializer;
import org.agrona.ExpandableDirectByteBuffer;

public class TypeList {

    public static final int WIRE_ID = 1;

    private final BinaryList.Out bytesOut;

    public TypeList() {
        bytesOut = new BinaryList.Out(WIRE_ID, new BufferBinaryOutput(new ExpandableDirectByteBuffer()));
    }

    public void add(Type type) {
        bytesOut.add(out -> TypeSerializer.instance.serialize(out, type));
    }

    public int size() {
        return bytesOut.size();
    }

    public BinaryList.Out getRawBytes() {
        return bytesOut;
    }

    public long byteLength() {
        return bytesOut.bytesWritten();
    }
}
