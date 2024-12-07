package com.ulyp.core.recorders.numeric;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.ObjectRecorder;
import org.jetbrains.annotations.NotNull;

/**
 * Number recorder. Handles floats
 */
public class FloatRecorder extends ObjectRecorder {

    public FloatRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return type == Float.class;
    }

    @Override
    public boolean supportsAsyncRecording() {
        return true;
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BytesIn input, ByIdTypeResolver typeResolver) {
        return new NumberRecord(objectType, String.valueOf(Float.intBitsToFloat(input.readInt())));
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        Float number = (Float) object;
        out.write(Float.floatToIntBits(number));
    }
}
