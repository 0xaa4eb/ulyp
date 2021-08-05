package com.ulyp.core;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface TypeResolver {

    @NotNull
    Type get(Object o);

    @NotNull
    Type get(Class<?> clazz);

    @NotNull
    Collection<Type> getAllKnownTypes();
}
