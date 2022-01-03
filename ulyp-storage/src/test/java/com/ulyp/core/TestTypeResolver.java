package com.ulyp.core;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TestTypeResolver implements TypeResolver {

    private final Map<Class<?>, Type> types = new HashMap<>();
    private int id = 0;

    @Override
    public @NotNull Type get(Object o) {
        return types.computeIfAbsent(o.getClass(), klazz -> Type.builder().id(id++).name(klazz.getName()).build());
    }

    @Override
    public @NotNull Type get(Class<?> clazz) {
        return types.computeIfAbsent(clazz, klazz -> Type.builder().id(id++).name(klazz.getName()).build());
    }

    @Override
    public @NotNull Collection<Type> getAllResolved() {
        return types.values();
    }
}
