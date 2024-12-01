package com.ulyp.core.serializers;

import com.ulyp.core.Type;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;

public class TypeSerializer implements Serializer<Type> {

    public static final TypeSerializer instance = new TypeSerializer();

    @Override
    public Type deserialize(BytesIn input) {
        return Type.builder()
                .id(input.readVarInt())
                .name(input.readString())
                .build();
    }

    @Override
    public void serialize(BytesOut out, Type m) {
        out.writeVarInt(m.getId());
        out.write(m.getName());
    }
}
