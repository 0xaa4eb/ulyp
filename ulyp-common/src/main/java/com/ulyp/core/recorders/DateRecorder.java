package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class DateRecorder extends ObjectRecorder {

    private static final String JAVA_UTIL_DATE_CLASS_NAME = Date.class.getName();

    protected DateRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Type type) {
        return type.getName().equals(JAVA_UTIL_DATE_CLASS_NAME);
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        return new DateRecord(objectType, input.readString());
    }

    @Override
    public void write(Object object, @NotNull Type objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        out.writeString(object.toString());
    }
}
