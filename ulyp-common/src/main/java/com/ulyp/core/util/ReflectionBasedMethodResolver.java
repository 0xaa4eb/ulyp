package com.ulyp.core.util;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.TestOnly;

import com.ulyp.core.Method;
import com.ulyp.core.Type;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.RecorderChooser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@TestOnly
public class ReflectionBasedMethodResolver {

    private static final AtomicLong idGenerator = new AtomicLong();

    private final ReflectionBasedTypeResolver typeResolver = new ReflectionBasedTypeResolver();

    public Method resolve(java.lang.reflect.Method method) {
        boolean returns = method.getReturnType() != Void.class;
        List<Type> parameters = Stream.of(method.getParameters())
            .map(a -> typeResolver.get(a.getType()))
            .collect(Collectors.toList());
        Type returnType = typeResolver.get(method.getReturnType());
        Type declaringType = typeResolver.get(method.getDeclaringClass());

        ObjectRecorder[] paramRecorders = RecorderChooser.getInstance().chooseForTypes(parameters);
        ObjectRecorder returnValueRecorder = RecorderChooser.getInstance().chooseForType(returnType);

        return Method.builder()
            .id(idGenerator.incrementAndGet())
            .name(method.getName())
            .isConstructor(false)
            .isStatic(Modifier.isStatic(method.getModifiers()))
            .returnsSomething(returns)
            .parameterRecorders(paramRecorders)
            .returnValueRecorder(returnValueRecorder)
            .declaringType(declaringType)
            .build();
    }
}
