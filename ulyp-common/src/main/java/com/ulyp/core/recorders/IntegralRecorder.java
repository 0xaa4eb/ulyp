package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BinaryInput;
import com.ulyp.core.bytes.BinaryOutput;
import org.jetbrains.annotations.NotNull;

/**
 * Number recorder. Handles everything including byte/short/int/long
 */
public class IntegralRecorder extends ObjectRecorder {

    protected IntegralRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return Long.class.isAssignableFrom(type) || Integer.class.isAssignableFrom(type) || Short.class.isAssignableFrom(type);
    }

    @Override
    public boolean supportsAsyncRecording() {
        return true;
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        return new NumberRecord(objectType, String.valueOf(input.readLong()));
    }

    @Override
    public void write(Object object, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        Number number = (Number) object;
        out.write(number.longValue());
    }
}
