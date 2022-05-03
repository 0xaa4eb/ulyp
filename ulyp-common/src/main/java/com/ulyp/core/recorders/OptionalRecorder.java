package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import com.ulyp.core.recorders.bytes.BinaryOutputAppender;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class OptionalRecorder extends ObjectRecorder {

    private static final String OPTIONAL_NAME = Optional.class.getName();

    protected OptionalRecorder(byte id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        return type.getName().equals(OPTIONAL_NAME);
    }

    @Override
    public ObjectRecord read(Type type, BinaryInput input, ByIdTypeResolver typeResolver) {
        boolean hasSomething = input.readBoolean();
        if (hasSomething) {
            ObjectRecord value = input.readObject(typeResolver);
            return new OptionalRecord(true, value, type);
        } else {
            return new OptionalRecord(false, null, type);
        }
    }

    @Override
    public void write(Object object, Type objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception {
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
