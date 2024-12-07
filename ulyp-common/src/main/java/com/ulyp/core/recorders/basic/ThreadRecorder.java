package com.ulyp.core.recorders.basic;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Records instances of {@link java.lang.Thread}
 */
@Slf4j
@ThreadSafe
public class ThreadRecorder extends ObjectRecorder {

    public ThreadRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return type == Thread.class;
    }

    @Override
    public boolean supportsAsyncRecording() {
        return true;
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BytesIn input, ByIdTypeResolver typeResolver) {
        IdentityObjectRecord identityRecord = (IdentityObjectRecord) ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance().read(objectType, input, typeResolver);
        long tid = input.readLong();
        String name = input.readString();
        return new ThreadRecord(objectType, identityRecord, tid, name);
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance().write(object, out, typeResolver);

        Thread thread = (Thread) object;
        out.write(thread.getId());
        out.write(thread.getName());
    }
}
