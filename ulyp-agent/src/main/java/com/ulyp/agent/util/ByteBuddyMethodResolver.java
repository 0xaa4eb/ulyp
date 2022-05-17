package com.ulyp.agent.util;

import com.ulyp.core.Method;
import com.ulyp.core.Type;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.RecorderChooser;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Converts byte buddy method description to internal domain class {@link Method}
 */
@Slf4j
public class ByteBuddyMethodResolver {

    private static final AtomicLong idGenerator = new AtomicLong();

    private final ByteBuddyTypeResolver typeResolver = new ByteBuddyTypeResolver();

    public Method resolve(TypeDescription instrumentedType, MethodDescription description) {
        boolean returns = !description.getReturnType().asGenericType().equals(TypeDescription.Generic.VOID);
        List<Type> parameters = description.getParameters().asTypeList().stream().map(typeResolver::resolve).collect(Collectors.toList());
        Type returnType = typeResolver.resolve(description.getReturnType());
        Type implementingType = typeResolver.resolve(instrumentedType.asGenericType());
        Type declaringType = typeResolver.resolve(description.getDeclaringType().asGenericType());

        ObjectRecorder[] paramRecorders = RecorderChooser.getInstance().chooseForTypes(parameters);
        ObjectRecorder returnValueRecorder = description.isConstructor() ?
                ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance() :
                RecorderChooser.getInstance().chooseForType(returnType);

        Method resolved = Method.builder()
                .id(idGenerator.incrementAndGet())
                .name(description.isConstructor() ? "<init>" : description.getActualName())
                .isConstructor(description.isConstructor())
                .isStatic(description.isStatic())
                .returnsSomething(returns)
                .parameterRecorders(paramRecorders)
                .returnValueRecorder(returnValueRecorder)
                .implementingType(implementingType)
                .declaringType(declaringType)
                .build();

        if (LoggingSettings.TRACE_ENABLED) {
            log.trace("Resolved {} to {}", description, resolved);
        }
        return resolved;
    }
}
