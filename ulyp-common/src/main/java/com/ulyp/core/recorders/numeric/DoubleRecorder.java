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
 * Number recorder. Handles doubles
 */
public class DoubleRecorder extends ObjectRecorder {

    public DoubleRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return type == Double.class;
    }

    @Override
    public boolean supportsAsyncRecording() {
        return true;
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BytesIn input, ByIdTypeResolver typeResolver) {
        return new NumberRecord(objectType, String.valueOf(Double.longBitsToDouble(input.readLong())));
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        Double number = (Double) object;
        out.write(Double.doubleToLongBits(number));
    }
}
