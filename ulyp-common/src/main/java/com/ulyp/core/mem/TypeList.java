package com.ulyp.core.mem;

import com.ulyp.core.Type;
import com.ulyp.core.bytes.BufferBytesOut;
import com.ulyp.core.serializers.TypeSerializer;
import lombok.Getter;
import org.agrona.ExpandableDirectByteBuffer;

@Getter
public class TypeList {

    public static final int WIRE_ID = 1;

    private final OutputBytesList bytes;

    public TypeList() {
        bytes = new OutputBytesList(WIRE_ID, new BufferBytesOut(new ExpandableDirectByteBuffer()));
    }

    public void add(Type type) {
        bytes.add(out -> TypeSerializer.instance.serialize(out, type));
    }

    public int size() {
        return bytes.size();
    }

    public long byteLength() {
        return bytes.bytesWritten();
    }
}
