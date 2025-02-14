package com.ulyp.core;

import com.ulyp.core.util.ConcurrentArrayList;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Is used at runtime to resolve object's type in order to determine what recorder should be used.
 */
@ThreadSafe
public interface TypeResolver {

    @NotNull
    Type get(Object o);

    @NotNull
    Type get(Class<?> clazz);

    Type getById(int id);

    @NotNull
    ConcurrentArrayList<Type> values();
}
