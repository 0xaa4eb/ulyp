package com.ulyp.core.printers;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.TypeTrait;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;

public class BooleanRecorder extends ObjectRecorder {

    protected BooleanRecorder(byte id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        return type.getTraits().contains(TypeTrait.BOOLEAN);
    }

    @Override
    public ObjectRepresentation read(Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        return new BooleanRepresentation(objectType, input.readBoolean());
    }

    @Override
    public void write(Object object, Type objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        Boolean value = (Boolean) object;
        out.writeBool(value);
    }
}
