package com.ulyp.core.recorders.basic;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecorder;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Method;

@Slf4j
@ThreadSafe
public class MethodRecorder extends ObjectRecorder {

    public MethodRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return type == Method.class;
    }

    @Override
    public boolean supportsAsyncRecording() {
        return true;
    }

    @Override
    public MethodRecord read(@NotNull Type objectType, BytesIn input, ByIdTypeResolver typeResolver) {
        Type declaringType = typeResolver.getType(input.readVarInt());
        String name = input.readString();
        return new MethodRecord(objectType, name, declaringType);
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        Method method = (Method) object;

        int typeId = typeResolver.get(method.getDeclaringClass()).getId();
        out.writeVarInt(typeId);
        out.write(method.getName());
    }
}
