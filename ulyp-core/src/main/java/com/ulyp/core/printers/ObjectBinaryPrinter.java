package com.ulyp.core.printers;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;

public abstract class ObjectBinaryPrinter {

    private final byte id;

    protected ObjectBinaryPrinter(byte id) {
        this.id = id;
    }

    public final byte getId() {
        return id;
    }

    public abstract ObjectRepresentation read(Type objectType, BinaryInput input, ByIdTypeResolver typeResolver);

    abstract boolean supports(Type type);

    public abstract void write(Object object, Type objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception;

    public void write(Object obj, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        write(obj, typeResolver.get(obj), out, typeResolver);
    }
}
