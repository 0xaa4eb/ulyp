package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import com.ulyp.core.recorders.bytes.BinaryOutputAppender;
import com.ulyp.core.util.TypeMatcher;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ToStringPrintingRecorder extends ObjectRecorder {

    private static final int TO_STRING_CALL_SUCCESS = 1;
    private static final int TO_STRING_CALL_FAIL = 0;

    private final Set<TypeMatcher> classesToPrint = new CopyOnWriteArraySet<>();

    protected ToStringPrintingRecorder(byte id) {
        super(id);
    }

    public void addClassesToPrint(Set<TypeMatcher> classNames) {
        this.classesToPrint.addAll(classNames);
    }

    @Override
    public boolean supports(Type type) {
        return classesToPrint.stream().anyMatch(matcher -> matcher.matches(type));
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BinaryInput input, ByIdTypeResolver typeResolver) {
        int result = input.readInt();
        if (result == TO_STRING_CALL_SUCCESS) {
            int identityHashCode = input.readInt();
            StringObjectRecord printed = (StringObjectRecord) input.readObject(typeResolver);
            return new PrintedObjectRecord(printed, objectType, identityHashCode);
        } else {
            return ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance().read(objectType, input, typeResolver);
        }
    }

    @Override
    public void write(Object object, @NotNull Type type, BinaryOutput out, TypeResolver typeResolver) throws Exception {
        try {
            String printed = object.toString();

            try (BinaryOutputAppender appender = out.appender()) {
                appender.append(TO_STRING_CALL_SUCCESS);
                appender.append(System.identityHashCode(object));
                appender.append(printed, typeResolver);
            }
        } catch (Throwable e) {
            try (BinaryOutputAppender appender = out.appender()) {
                appender.append(TO_STRING_CALL_FAIL);
                ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance().write(object, appender, typeResolver);
            }
        }
    }
}
