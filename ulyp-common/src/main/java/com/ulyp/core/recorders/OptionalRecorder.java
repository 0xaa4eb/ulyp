package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import com.ulyp.core.recorders.bytes.BinaryOutputAppender;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Slf4j
public class OptionalRecorder extends ObjectRecorder {

    protected OptionalRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return type == Optional.class;
    }

    @Override
    public boolean supportsAsyncRecording() {
        return true;
    }

    @Override
    public ObjectRecord read(@NotNull Type type, BinaryInput input, ByIdTypeResolver typeResolver) {
        boolean hasSomething = input.readBoolean();
        if (hasSomething) {
            ObjectRecord value = input.readObject(typeResolver);
            return new OptionalRecord(true, value, type);
        } else {
            return new OptionalRecord(false, null, type);
        }
    }

    @Override
    public void write(Object object, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        try (BinaryOutputAppender appender = out.appender()) {
            Optional<?> optional = (Optional<?>) object;
            boolean hasSomething = optional.isPresent();
            appender.writeBool(hasSomething);
            if (hasSomething) {
                Object value = optional.get();
                appender.append(value, typeResolver);
            }
        }
    }
}
