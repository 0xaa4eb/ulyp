package com.ulyp.core.recorders;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.util.TypeMatcher;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PrintingRecorder extends ObjectRecorder {

    private static final int TO_STRING_CALL_SUCCESS = 1;
    private static final int TO_STRING_CALL_FAIL = 0;

    private final List<TypeMatcher> printedTypeMatchers = new CopyOnWriteArrayList<>();

    protected PrintingRecorder(byte id) {
        super(id);
    }

    public void addTypeMatchers(List<TypeMatcher> classNames) {
        this.printedTypeMatchers.addAll(classNames);
    }

    @Override
    public boolean supports(Class<?> type) {
        if (printedTypeMatchers.isEmpty()) {
            return false;
        }

        return printedTypeMatchers.stream().anyMatch(matcher -> matcher.matches(Type.builder().name(type.getName()).build()));
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BytesIn input, ByIdTypeResolver typeResolver) {
        int result = input.readInt();
        if (result == TO_STRING_CALL_SUCCESS) {
            int identityHashCode = input.readInt();
            String printed = input.readString();
            return new PrintedObjectRecord(printed, objectType, identityHashCode);
        } else {
            return ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance().read(objectType, input, typeResolver);
        }
    }

    @Override
    public void write(Object object, BytesOut out, TypeResolver typeResolver) throws Exception {
        try {
            String printed = object.toString();

            out.write(TO_STRING_CALL_SUCCESS);
            out.write(System.identityHashCode(object));
            out.write(printed);
        } catch (Throwable e) {
            out.write(TO_STRING_CALL_FAIL);
            ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance().write(object, out, typeResolver);
        }
    }
}
