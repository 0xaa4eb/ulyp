package com.ulyp.core.recorders.arrays;

import org.jetbrains.annotations.NotNull;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.TypeTrait;
import com.ulyp.core.recorders.IdentityRecorder;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import com.ulyp.core.recorders.bytes.BinaryOutputAppender;

public class ByteArrayRecorder extends IdentityRecorder {

    public ByteArrayRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Type type) {
        return type.getTraits().contains(TypeTrait.PRIMITIVE_BYTE_ARRAY);
    }

    @Override
    public ObjectRecord read(@NotNull Type type, BinaryInput input, ByIdTypeResolver typeResolver) {
        ObjectRecord identityRecord = super.read(type, input, typeResolver);
        return new ByteArrayRecord(type, identityRecord.hashCode(), input.readInt());
    }

    @Override
    public void write(Object object, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        try (BinaryOutputAppender appender = out.appender()) {
            super.write(object, appender, typeResolver);
            byte[] array = (byte[]) object;
            appender.append(array.length);
        }
    }
}
