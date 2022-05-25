package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.TypeTrait;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import com.ulyp.core.recorders.bytes.BinaryOutputAppender;
import org.jetbrains.annotations.NotNull;

public class ThrowableRecorder extends ObjectRecorder {

    protected ThrowableRecorder(byte id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        return type.getTraits().contains(TypeTrait.THROWABLE);
    }

    @Override
    public ObjectRecord read(@NotNull Type type, BinaryInput input, ByIdTypeResolver typeResolver) {
        return new ThrowableRecord(type, input.readObject(typeResolver));
    }

    @Override
    public void write(Object object, Type classDescription, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        try (BinaryOutputAppender appender = out.appender()) {
            Throwable t = (Throwable) object;
            appender.append(t.getMessage(), typeResolver);
        }
    }
}
