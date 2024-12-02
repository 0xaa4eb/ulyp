package com.ulyp.core.recorders.kotlin;

import com.ulyp.core.ByIdTypeResolver;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.ObjectRecorder;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KtTripleRecorder extends ObjectRecorder {

    private static final String KT_TRIPLE_CLASS_NAME = "kotlin.Triple";
    private static final String KT_TRIPLE_FIRST_FIELD_NAME = "getFirst";
    private static final String KT_TRIPLE_SECOND_FIELD_NAME = "getSecond";
    private static final String KT_TRIPLE_THIRD_FIELD_NAME = "getThird";

    private final Map<Class<?>, ClassMethodHandles> methodHandles = new ConcurrentHashMap<>();

    public KtTripleRecorder(byte id) {
        super(id);
    }

    @Override
    public boolean supports(Class<?> type) {
        if (!type.getName().equals(KT_TRIPLE_CLASS_NAME)) {
            return false;
        }

        Method[] declaredMethods = type.getDeclaredMethods();
        return Arrays.stream(declaredMethods).anyMatch(
                method -> method.getName().equals(KT_TRIPLE_FIRST_FIELD_NAME)
                        && method.getReturnType() == Object.class
                        && method.getParameterCount() == 0)
                && Arrays.stream(declaredMethods).anyMatch(
                        method -> method.getName().equals(KT_TRIPLE_SECOND_FIELD_NAME)
                                && method.getReturnType() == Object.class
                                && method.getParameterCount() == 0)
                && Arrays.stream(declaredMethods).anyMatch(
                        method -> method.getName().equals(KT_TRIPLE_THIRD_FIELD_NAME)
                                && method.getReturnType() == Object.class
                                && method.getParameterCount() == 0);
    }

    @Override
    public boolean supportsAsyncRecording() {
        return false;
    }

    @Override
    public ObjectRecord read(@NotNull Type objectType, BytesIn input, ByIdTypeResolver typeResolver) {
        return new KtTripleRecord(
                objectType,
                input.readObject(typeResolver),
                input.readObject(typeResolver),
                input.readObject(typeResolver)
        );
    }

    @Override
    public void write(Object pair, BytesOut out, TypeResolver typeResolver) throws Exception {
        ClassMethodHandles methodHandles = getMethodHandles(pair.getClass());
        Object first = methodHandles.extractFirst(pair);
        Object second = methodHandles.extractSecond(pair);
        Object third = methodHandles.extractThird(pair);
        out.write(first, typeResolver);
        out.write(second, typeResolver);
        out.write(third, typeResolver);
    }

    private ClassMethodHandles getMethodHandles(Class<?> pairClass) {
        ClassMethodHandles methodHandles = this.methodHandles.get(pairClass);
        if (methodHandles != null) {
            return methodHandles;
        }

        return this.methodHandles.computeIfAbsent(pairClass, clazz -> {
            MethodHandles.Lookup publicLookup = MethodHandles.lookup();
            MethodHandle firstMh;
            try {
                firstMh = publicLookup.findVirtual(pairClass, KT_TRIPLE_FIRST_FIELD_NAME, MethodType.methodType(Object.class));
            } catch (IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            MethodHandle secondMh;
            try {
                secondMh = publicLookup.findVirtual(pairClass, KT_TRIPLE_SECOND_FIELD_NAME, MethodType.methodType(Object.class));
            } catch (IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            MethodHandle thirdMh;
            try {
                thirdMh = publicLookup.findVirtual(pairClass, KT_TRIPLE_THIRD_FIELD_NAME, MethodType.methodType(Object.class));
            } catch (IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            return new ClassMethodHandles(firstMh, secondMh, thirdMh);
        });
    }

    private static class ClassMethodHandles {
        final MethodHandle firstMh;
        final MethodHandle secondMh;
        final MethodHandle thirdMh;

        private ClassMethodHandles(MethodHandle firstMh, MethodHandle secondMh, MethodHandle thirdMh) {
            this.firstMh = firstMh;
            this.secondMh = secondMh;
            this.thirdMh = thirdMh;
        }

        private Object extractFirst(Object pair) {
            try {
                return firstMh.invoke(pair);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        private Object extractSecond(Object pair) {
            try {
                return secondMh.invoke(pair);
            } catch (Throwable e) {
                // TODO should bypass Throwable, but catch others and rethrow as RecordingException
                throw new RuntimeException(e);
            }
        }

        private Object extractThird(Object pair) {
            try {
                return thirdMh.invoke(pair);
            } catch (Throwable e) {
                // TODO should bypass Throwable, but catch others and rethrow as RecordingException
                throw new RuntimeException(e);
            }
        }
    }
}
