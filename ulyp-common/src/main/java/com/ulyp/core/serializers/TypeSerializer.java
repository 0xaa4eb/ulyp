package com.ulyp.core.serializers;

import com.ulyp.core.Type;
import com.ulyp.core.bytes.BinaryInput;
import com.ulyp.core.bytes.BinaryOutput;

public class TypeSerializer implements Serializer<Type> {

    public static final TypeSerializer instance = new TypeSerializer();

    @Override
    public Type deserialize(BinaryInput input) {
        return Type.builder()
                .id(input.readInt())
                .name(input.readString())
                .build();
    }

    @Override
    public void serialize(BinaryOutput out, Type m) {
        out.write(m.getId());
        out.write(m.getName());
    }
}
