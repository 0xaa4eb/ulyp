package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;

import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class ThrowableRecorder extends ObjectRecorder {

    protected ThrowableRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return Throwable.class.isAssignableFrom(type);
    }

    @Override
    public boolean supportsAsyncRecording() {
        return true;
    }

    @Override
    public ObjectRecord read(@NotNull Type type, BytesIn input, ByIdTypeResolver typeResolver) {
        return new ThrowableRecord(type, input.readObject(typeResolver));
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        Throwable t = (Throwable) object;
        out.write(t.getMessage(), typeResolver);
    }
}
