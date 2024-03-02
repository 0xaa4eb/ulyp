package com.ulyp.core.mem;

import com.ulyp.core.Method;
import com.ulyp.core.bytes.BufferBytesOut;
import com.ulyp.core.serializers.MethodSerializer;
import lombok.Getter;
import org.agrona.ExpandableDirectByteBuffer;

@Getter
public class MethodList {

    public static final int WIRE_ID = 3;

    private final OutputBytesList bytes;

    public MethodList() {
        bytes = new OutputBytesList(WIRE_ID, new BufferBytesOut(new ExpandableDirectByteBuffer()));
    }

    public void add(Method method) {
        bytes.add(out -> MethodSerializer.instance.serialize(out, method));
    }

    public int size() {
        return bytes.size();
    }

    public int byteLength() {
        return bytes.bytesWritten();
    }
}
