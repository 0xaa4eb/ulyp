package com.ulyp.core.printers;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;

public class IdentityPrinter extends ObjectBinaryPrinter {

    protected IdentityPrinter(byte id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        return true;
    }

    @Override
    public ObjectRepresentation read(Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        int identityHashCode = input.readInt();
        return new IdentityObjectRepresentation(objectType, identityHashCode);
    }

    @Override
    public void write(Object object, Type objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        out.writeInt(System.identityHashCode(object));
    }
}
