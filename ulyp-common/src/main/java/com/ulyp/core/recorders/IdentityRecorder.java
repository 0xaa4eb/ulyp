package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import org.jetbrains.annotations.NotNull;

public class IdentityRecorder extends ObjectRecorder {

    protected IdentityRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return true;
    }

    @Override
    public boolean supportsAsyncRecording() {
        return true;
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BytesIn input, ByIdTypeResolver typeResolver) {
        int identityHashCode = input.readInt();
        return new IdentityObjectRecord(objectType, identityHashCode);
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        out.write(System.identityHashCode(object));
    }
}
