package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import org.jetbrains.annotations.NotNull;

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
    public ObjectRecord read(@NotNull Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        int nestedTypeId = input.readInt();
        byte recorderId = input.readByte();
        BinaryInput binaryInput = input.readBytes();
        Type type = typeResolver.getType(nestedTypeId);
        return ObjectRecorderRegistry.recorderForId(recorderId).read(type, binaryInput, typeResolver);
    }

    @Override
    public void write(Object object, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        QueuedRecordedObject recordedObject = (QueuedRecordedObject) object;
        out.write(recordedObject.getType().getId());
        out.write(recordedObject.getRecorderId());
        out.write(recordedObject.getData());
    }
}
