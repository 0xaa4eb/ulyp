package com.ulyp.agent.util;

import com.ulyp.core.Converter;
import com.ulyp.core.Method;
import com.ulyp.core.Type;
import com.ulyp.core.util.LoggingSettings;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

/**
 * Converts byte buddy method description to internal domain class {@link Method}
 */
@Slf4j
public class ByteBuddyMethodConverter implements Converter<MethodDescription, Method> {

    private final ByteBuddyTypeConverter declaringTypeConverter;

    public ByteBuddyMethodConverter(ByteBuddyTypeConverter declaringTypeConverter) {
        this.declaringTypeConverter = declaringTypeConverter;
    }

    public Method convert(MethodDescription description) {
        boolean returns = !description.getReturnType().asGenericType().equals(TypeDescription.Generic.VOID);
        Type declaringType = declaringTypeConverter.convert(description.getDeclaringType().asGenericType());
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
                .name(name)
                .constructor(description.isConstructor())
                .isStatic(description.isStatic())
                .returnsSomething(returns)
                .type(declaringType)
                .build();

        return resolved;
    }
}
