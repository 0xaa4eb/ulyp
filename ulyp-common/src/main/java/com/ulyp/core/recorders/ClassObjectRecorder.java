package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import com.ulyp.core.util.LoggingSettings;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class ClassObjectRecorder extends ObjectRecorder {

    protected ClassObjectRecorder(byte id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        return type.isClassObject();
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        return new ClassObjectRecord(objectType, typeResolver.getType(input.readLong()));
    }

    @Override
    public void write(Object object, Type objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        Class<?> clazz = (Class<?>) object;

        long typeId = typeResolver.get(clazz).getId();
        out.writeLong(typeId);

        if (LoggingSettings.TRACE_ENABLED) {
            log.trace("Writing typeId={} for {}", typeId, object);
        }
    }
}
