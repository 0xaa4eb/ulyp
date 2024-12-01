package com.ulyp.core.recorders.collections;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.recorders.ObjectRecord;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Set;

@Slf4j
@ThreadSafe
public class SetRecorder extends CollectionRecorder {

    public SetRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return super.supports(type) && Set.class.isAssignableFrom(type);
    }

    @Override
    public ObjectRecord read(@NotNull Type type, BytesIn input, ByIdTypeResolver typeResolver) {
        return read(type, CollectionType.SET, input, typeResolver);
    }
}
