package com.ulyp.agent.util;

import com.ulyp.core.Method;
import com.ulyp.core.Type;
import com.ulyp.core.util.LoggingSettings;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Converts byte buddy method description to internal domain class {@link Method}
 */
@Slf4j
public class ByteBuddyMethodResolver {

    public static final ByteBuddyMethodResolver INSTANCE = new ByteBuddyMethodResolver(ByteBuddyTypeConverter.SUPER_TYPE_DERIVING_INSTANCE);

    private final ByteBuddyTypeConverter typeConverter;
    private final AtomicInteger idGenerator = new AtomicInteger(-1);

    public ByteBuddyMethodResolver(ByteBuddyTypeConverter typeConverter) {
        this.typeConverter = typeConverter;
    }

    public Method resolve(MethodDescription description) {
        boolean returns = !description.getReturnType().asGenericType().equals(TypeDescription.Generic.VOID);
        Type declaringType = typeConverter.convert(description.getDeclaringType().asGenericType());
        String actualName = description.getActualName();
        String name;
        if (description.isConstructor()) {
            name = "<init>";
        } else if (actualName.isEmpty() && description.isStatic()) {
            name = "<clinit>";
        } else {
            name = description.getActualName();
        }

        Method resolved = Method.builder()
                .id(idGenerator.incrementAndGet())
                .name(name)
                .isConstructor(description.isConstructor())
                .isStatic(description.isStatic())
                .returnsSomething(returns)
                .declaringType(declaringType)
                .build();

        if (LoggingSettings.TRACE_ENABLED) {
            log.trace("Resolved {} to {}", description, resolved);
        }
        return resolved;
    }
}
