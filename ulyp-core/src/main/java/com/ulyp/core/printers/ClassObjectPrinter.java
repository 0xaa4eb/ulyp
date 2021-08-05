package com.ulyp.core.printers;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;

public class ClassObjectPrinter extends ObjectBinaryPrinter {

    protected ClassObjectPrinter(byte id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        return type.isClassObject();
    }

    @Override
    public ObjectRepresentation read(Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {

        return new ClassObjectRepresentation(objectType, typeResolver.getType(input.readInt()));
    }

    @Override
    public void write(Object object, Type objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        Class<?> clazz = (Class<?>) object;

        out.writeLong(typeResolver.get(clazz).getId());
    }
}
