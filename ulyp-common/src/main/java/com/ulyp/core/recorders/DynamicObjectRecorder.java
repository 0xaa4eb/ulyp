package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.TypeTrait;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import com.ulyp.core.recorders.bytes.BinaryOutputAppender;
import org.jetbrains.annotations.NotNull;

public class DynamicObjectRecorder extends ObjectRecorder {

    protected DynamicObjectRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Type type) {
        return type.getTraits().contains(TypeTrait.INTERFACE) || type.isExactlyJavaLangObject() || type.getTraits().contains(TypeTrait.TYPE_VAR);
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        byte recorderId = input.readByte();
        return ObjectRecorderRegistry.recorderForId(recorderId).read(objectType, input, typeResolver);
    }

    @Override
    public void write(Object object, @NotNull Type objectType, BinaryOutput out, TypeResolver typeResolver) throws Exception {
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
