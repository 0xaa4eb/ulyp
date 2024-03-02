package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.util.LoggingSettings;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class BooleanRecorder extends ObjectRecorder {

    protected BooleanRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return type == boolean.class || type == Boolean.class;
    }

    @Override
    public boolean supportsAsyncRecording() {
        return true;
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BytesIn input, ByIdTypeResolver typeResolver) {
        return new BooleanRecord(objectType, input.readBoolean());
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        Boolean value = (Boolean) object;
        out.write((boolean) value);
        if (LoggingSettings.TRACE_ENABLED) {
            log.trace("Writing {}", object);
        }
    }
}
