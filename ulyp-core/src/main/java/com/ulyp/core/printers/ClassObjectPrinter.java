package com.ulyp.core.printers;

import com.ulyp.core.DecodingContext;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;

public class ClassObjectPrinter extends ObjectBinaryPrinter {

    protected ClassObjectPrinter(byte id) {
        super(id);
    }

    @Override
    boolean supports(TypeInfo type) {
        return type.isClassObject();
    }

    @Override
    public ObjectRepresentation read(TypeInfo objectType, BinaryInput input, DecodingContext decodingContext) {

        return new ClassObjectRepresentation(objectType, decodingContext.getType(input.readInt()));
    }

    @Override
    public void write(Object object, TypeInfo objectType, BinaryOutput out, TypeResolver runtime) throws Exception {
        Class<?> clazz = (Class<?>) object;

        out.writeInt(runtime.get(clazz).getId());
    }
}
