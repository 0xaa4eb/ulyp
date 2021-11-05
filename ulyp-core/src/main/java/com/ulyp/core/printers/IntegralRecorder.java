package com.ulyp.core.printers;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.TypeTrait;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;

// Handles everything including byte/short/int/long
public class IntegralRecorder extends ObjectRecorder {

    protected IntegralRecorder(byte id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        return type.getTraits().contains(TypeTrait.INTEGRAL);
    }

    @Override
    public ObjectRecord read(Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        return new NumberObjectRecord(objectType, String.valueOf(input.readLong()));
    }

    @Override
    public void write(Object object, Type objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        Number number = (Number) object;
        out.writeLong(number.longValue());
    }
}
