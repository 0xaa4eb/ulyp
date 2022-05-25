package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import com.ulyp.core.recorders.bytes.BinaryOutputAppender;
import org.jetbrains.annotations.NotNull;

public class DynamicObjectRecorder extends ObjectRecorder {

    protected DynamicObjectRecorder(byte id) {
        super(id);
    }

    @Override
    boolean supports(Type type) {
        return type.isInterface() || type.isExactlyJavaLangObject() || type.isTypeVar();
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        byte recorderId = input.readByte();
        return ObjectRecorderRegistry.recorderForId(recorderId).read(objectType, input, typeResolver);
    }

    @Override
    public void write(Object object, Type objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        ObjectRecorder recorder = objectType.getSuggestedRecorder();
        if (recorder.getId() == getId()) {
            recorder = ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance();
        }

        try (BinaryOutputAppender appender = out.appender()) {
            appender.append(recorder.getId());
            recorder.write(object, appender, typeResolver);
        }
    }
}
