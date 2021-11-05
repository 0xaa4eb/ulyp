package com.ulyp.core.printers;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.printers.bytes.BinaryInput;
import com.ulyp.core.printers.bytes.BinaryOutput;

public class EnumRecorder extends ObjectRecorder {

    protected EnumRecorder(byte id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        return type.isEnum();
    }

    @Override
    public ObjectRepresentation read(Type type, BinaryInput input, ByIdTypeResolver typeResolver) {
        return new EnumRepresentation(type, input.readString());
    }

    @Override
    public void write(Object object, Type objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        out.writeString(((Enum<?>) object).name());
    }
}
