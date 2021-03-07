package com.ulyp.agent.util;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.log.AgentLogManager;
import com.ulyp.core.log.Logger;
import com.ulyp.core.printers.TypeInfo;
import com.ulyp.core.printers.UnknownTypeInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ByteBuddyTypeResolver implements TypeResolver {

    private static final Logger LOGGER = AgentLogManager.getLogger(ByteBuddyTypeResolver.class);

    private static class InstanceHolder {
        private static final ByteBuddyTypeResolver context = new ByteBuddyTypeResolver();
    }

    public static TypeResolver getInstance() {
        return InstanceHolder.context;
    }

    private final Map<Class<?>, TypeInfo> classDescriptionMap = new ConcurrentHashMap<>();

    @NotNull
    @Override
    public TypeInfo get(Object o) {
        if (o != null) {
            return classDescriptionMap.computeIfAbsent(
                    o.getClass(),
                    ByteBuddyTypeInfo::of
            );
        } else {
            return UnknownTypeInfo.getInstance();
        }
    }

    @NotNull
    @Override
    public TypeInfo get(Class<?> clazz) {
        if (clazz != null) {
            return classDescriptionMap.computeIfAbsent(
                    clazz,
                    ByteBuddyTypeInfo::of
            );
        } else {
            return UnknownTypeInfo.getInstance();
        }
    }

    @NotNull
    @Override
    public Collection<TypeInfo> getAllKnownTypes() {
        return classDescriptionMap.values();
    }
}
