package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Number recorder. Handles everything including byte/short/int/long
 */
@ThreadSafe
public class IntegralRecorder extends ObjectRecorder {

    protected IntegralRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return Long.class == type || Integer.class == type || Short.class == type;
    }

    @Override
    public boolean supportsAsyncRecording() {
        return true;
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BytesIn input, ByIdTypeResolver typeResolver) {
        return new NumberRecord(objectType, String.valueOf(input.readLong()));
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        Number number = (Number) object;
        out.write(number.longValue());
    }
}
