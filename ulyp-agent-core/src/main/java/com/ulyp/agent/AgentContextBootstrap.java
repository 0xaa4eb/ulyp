package com.ulyp.agent;

import com.ulyp.core.Converter;
import com.ulyp.core.Method;
import com.ulyp.core.Type;
import lombok.Builder;
import lombok.Getter;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;

@Getter
@Builder
public class AgentContextBootstrap {

    private final Converter<TypeDescription.Generic, Type> typeConverter;
    private final Converter<MethodDescription, Method> methodConverter;
}
