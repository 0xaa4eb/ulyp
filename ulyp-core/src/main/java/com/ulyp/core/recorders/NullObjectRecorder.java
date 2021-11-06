package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;

public class NullObjectRecorder extends ObjectRecorder {

    protected NullObjectRecorder(byte id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        return false;
    }

    @Override
    public ObjectRecord read(Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        // still need to read as this printer may be used inside another printer
        input.readBoolean();
        return NullObjectRecord.getInstance();
    }

    @Override
    public void write(Object object, Type classDescription, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        out.writeBool(false);
    }
}
