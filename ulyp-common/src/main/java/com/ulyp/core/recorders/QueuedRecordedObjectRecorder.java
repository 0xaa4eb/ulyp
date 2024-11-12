package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class QueuedRecordedObjectRecorder extends ObjectRecorder {

    protected QueuedRecordedObjectRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return type == QueuedRecordedObject.class;
    }

    @Override
    public boolean supportsAsyncRecording() {
        return true;
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BytesIn input, ByIdTypeResolver typeResolver) {
        int nestedTypeId = input.readInt();
        byte recorderId = input.readByte();
        BytesIn bytesIn = input.readBytes();
        Type type = typeResolver.getType(nestedTypeId);
        return ObjectRecorderRegistry.recorderForId(recorderId).read(type, bytesIn, typeResolver);
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        QueuedRecordedObject recordedObject = (QueuedRecordedObject) object;
        out.write(recordedObject.getType().getId());
        out.write(recordedObject.getRecorderId());
        out.write(recordedObject.getData());
    }
}
