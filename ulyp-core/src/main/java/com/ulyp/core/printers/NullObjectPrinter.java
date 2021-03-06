package com.ulyp.core.printers;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;

public class NullObjectPrinter extends ObjectBinaryPrinter {

    protected NullObjectPrinter(byte id) {
        super(id);
    }

    @Override
    boolean supports(TypeInfo type) {
        return false;
    }

    @Override
    public ObjectRepresentation read(TypeInfo objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        // still need to read as this printer may be used inside another printer
        input.readBoolean();
        return NullObjectRepresentation.getInstance();
    }

    @Override
    public void write(Object object, TypeInfo classDescription, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        out.writeBool(false);
    }
}
