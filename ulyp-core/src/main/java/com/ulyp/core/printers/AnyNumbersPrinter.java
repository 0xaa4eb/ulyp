package com.ulyp.core.printers;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;

public class AnyNumbersPrinter extends ObjectBinaryPrinter {

    protected AnyNumbersPrinter(byte id) {
        super(id);
    }

    @Override
    boolean supports(TypeInfo type) {
        return type.getTraits().contains(TypeTrait.NUMBER) || type.getTraits().contains(TypeTrait.PRIMITIVE);
    }

    @Override
    public ObjectRepresentation read(TypeInfo objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        return new NumberObjectRepresentation(objectType, input.readString());
    }

    @Override
    public void write(Object object, TypeInfo objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        out.writeString(object.toString());
    }
}
