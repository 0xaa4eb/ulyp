package com.ulyp.core;

import com.ulyp.core.util.ConcurrentArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface TypeResolver {

    @NotNull
    Type get(Object o);

    @NotNull
    Type get(Class<?> clazz);

    @NotNull
    Collection<Type> getAllResolved();

    @NotNull
    ConcurrentArrayList<Type> getAllResolvedAsConcurrentList();
}
