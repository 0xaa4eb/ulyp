package com.ulyp.core.recorders.arrays;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.IdentityRecorder;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.bytes.BinaryInput;
import com.ulyp.core.bytes.BinaryOutput;

import org.jetbrains.annotations.NotNull;

public class ByteArrayRecorder extends IdentityRecorder {

    public ByteArrayRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supportsAsyncRecording() {
        return false;
    }

    @Override
    public boolean supports(Class<?> type) {
        return type == byte[].class;
    }

    @Override
    public ObjectRecord read(@NotNull Type type, BinaryInput input, ByIdTypeResolver typeResolver) {
        ObjectRecord identityRecord = super.read(type, input, typeResolver);
        return new ByteArrayRecord(type, identityRecord.hashCode(), input.readInt());
    }

    @Override
    public void write(Object object, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        super.write(object, out, typeResolver);
        byte[] array = (byte[]) object;
        out.write(array.length);
    }
}
