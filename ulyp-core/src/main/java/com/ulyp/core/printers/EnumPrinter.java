package com.ulyp.core.printers;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.DecodingContext;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;

public class EnumPrinter extends ObjectBinaryPrinter {

    protected EnumPrinter(byte id) {
        super(id);
    }

    @Override
    boolean supports(TypeInfo type) {
        return type.isEnum();
    }

    @Override
    public ObjectRepresentation read(TypeInfo type, BinaryInput input, DecodingContext decodingContext) {
        return new EnumRepresentation(type, input.readString());
    }

    @Override
    public void write(Object object, TypeInfo objectType, BinaryOutput out, TypeResolver runtime) throws Exception {
        out.writeString(((Enum<?>) object).name());
    }
}
