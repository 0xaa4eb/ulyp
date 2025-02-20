package com.ulyp.core.recorders.basic;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.ObjectRecorder;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Optional;

@Slf4j
@ThreadSafe
public class OptionalRecorder extends ObjectRecorder {

    public OptionalRecorder(byte id) {
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
    public OptionalRecord read(@NotNull Type type, BytesIn input, ByIdTypeResolver typeResolver) {
        boolean hasSomething = input.readBoolean();
        if (hasSomething) {
            ObjectRecord value = input.readObject(typeResolver);
            return new OptionalRecord(true, value, type);
        } else {
            return new OptionalRecord(false, null, type);
        }
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        Optional<?> optional = (Optional<?>) object;
        boolean hasSomething = optional.isPresent();
        out.write(hasSomething);
        if (hasSomething) {
            Object value = optional.get();
            out.write(value, typeResolver);
        }
    }
}
