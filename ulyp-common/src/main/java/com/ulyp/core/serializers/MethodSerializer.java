package com.ulyp.core.serializers;

import com.ulyp.core.Method;
import com.ulyp.core.bytes.BinaryInput;
import com.ulyp.core.bytes.BinaryOutput;

public class MethodSerializer implements Serializer<Method> {

    public static final MethodSerializer instance = new MethodSerializer();

    @Override
    public Method deserialize(BinaryInput input) {
        return Method.builder()
                .id(input.readInt())
                .returnsSomething(input.readBoolean())
                .isStatic(input.readBoolean())
                .isConstructor(input.readBoolean())
                .name(input.readString())
                .declaringType(TypeSerializer.instance.deserialize(input))
                .build();
    }

    @Override
    public void serialize(BinaryOutput out, Method m) {
        out.write(m.getId());
        out.write(m.returnsSomething());
        out.write(m.isStatic());
        out.write(m.isConstructor());
        out.write(m.getName());
        TypeSerializer.instance.serialize(out, m.getDeclaringType());
    }
}
