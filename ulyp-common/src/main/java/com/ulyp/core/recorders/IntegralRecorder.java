package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.TypeTrait;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import org.jetbrains.annotations.NotNull;

/**
 * Number recorder. Handles everything including byte/short/int/long
 */
public class IntegralRecorder extends ObjectRecorder {

    protected IntegralRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Type type) {
        return type.getTraits().contains(TypeTrait.INTEGRAL);
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        return new NumberRecord(objectType, String.valueOf(input.readLong()));
    }

    @Override
    public void write(Object object, @NotNull Type objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        Number number = (Number) object;
        out.writeLong(number.longValue());
    }
}
