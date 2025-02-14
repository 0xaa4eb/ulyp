package com.ulyp.core.recorders.basic;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.util.LoggingSettings;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;

@Slf4j
@ThreadSafe
public class ClassRecorder extends ObjectRecorder {

    public ClassRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return type == Class.class;
    }

    @Override
    public boolean supportsAsyncRecording() {
        return true;
    }

    @Override
    public ClassRecord read(@NotNull Type objectType, BytesIn input, ByIdTypeResolver typeResolver) {
        return new ClassRecord(objectType, typeResolver.getType(input.readVarInt()));
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        Class<?> clazz = (Class<?>) object;

        int typeId = typeResolver.get(clazz).getId();
        out.writeVarInt(typeId);

        if (LoggingSettings.TRACE_ENABLED) {
            log.trace("Writing typeId={} for {}", typeId, object);
        }
    }
}
