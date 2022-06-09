package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import org.jetbrains.annotations.NotNull;

public class IdentityRecorder extends ObjectRecorder {

    protected IdentityRecorder(byte id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        return true;
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        int identityHashCode = input.readInt();
        return new IdentityObjectRecord(objectType, identityHashCode);
    }

    @Override
    public void write(Object object, @NotNull Type objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        out.writeInt(System.identityHashCode(object));
    }
}
