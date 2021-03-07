package com.ulyp.core.printers;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.DecodingContext;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;

// Handles everything including byte/short/int/long
public class IntegralPrinter extends ObjectBinaryPrinter {

    protected IntegralPrinter(byte id) {
        super(id);
    }

    @Override
    boolean supports(TypeInfo type) {
        return type.getTraits().contains(TypeTrait.INTEGRAL);
    }

    @Override
    public ObjectRepresentation read(TypeInfo objectType, BinaryInput input, DecodingContext decodingContext) {
        return new NumberObjectRepresentation(objectType, String.valueOf(input.readLong()));
    }

    @Override
    public void write(Object object, TypeInfo objectType, BinaryOutput out, TypeResolver runtime) throws Exception {
        Number number = (Number) object;
        out.writeLong(number.longValue());
    }
}
