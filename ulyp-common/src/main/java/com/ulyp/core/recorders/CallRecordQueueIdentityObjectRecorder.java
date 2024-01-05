package com.ulyp.core.recorders;

import org.jetbrains.annotations.NotNull;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;

public class CallRecordQueueIdentityObjectRecorder extends ObjectRecorder {

    protected CallRecordQueueIdentityObjectRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        return type == CallRecordQueueIdentityObject.class;
    }

    @Override
    public boolean supportsAsyncRecording() {
        return true;
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        int nestedTypeId = input.readInt();
        int identityHashCode = input.readInt();
        return new IdentityObjectRecord(typeResolver.getType(nestedTypeId), identityHashCode);
    }

    @Override
    public void write(Object object, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        CallRecordQueueIdentityObject identityObject = (CallRecordQueueIdentityObject) object;
        out.writeInt(identityObject.getType().getId());
        out.writeInt(identityObject.getIdentityHashCode());
    }
}
