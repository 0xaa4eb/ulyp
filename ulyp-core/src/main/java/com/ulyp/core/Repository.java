package com.ulyp.core;

import java.util.Collection;
import java.util.function.Supplier;

public interface Repository<T> extends ReadableRepository<T> {

    T computeIfAbsent(long id, Supplier<T> supplier);

    void store(long id, T value);

    Collection<T> values();
}
