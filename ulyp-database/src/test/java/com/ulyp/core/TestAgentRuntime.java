package com.ulyp.core;

import com.ulyp.core.printers.TypeInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TestAgentRuntime implements AgentRuntime {

    private final Map<Class<?>, TypeInfo> types = new HashMap<>();
    private int id = 0;

    @Override
    public @NotNull TypeInfo get(Object o) {
        return types.computeIfAbsent(o.getClass(), klazz -> new NameOnlyTypeInfo(id++, klazz.getName()));
    }

    @Override
    public @NotNull TypeInfo get(Class<?> clazz) {
        return types.computeIfAbsent(clazz, klazz -> new NameOnlyTypeInfo(id++, klazz.getName()));
    }

    @Override
    public @NotNull Collection<TypeInfo> getAllKnownTypes() {
        return types.values();
    }
}
