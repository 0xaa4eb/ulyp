package com.ulyp.core.repository;

import java.util.Collection;
import java.util.function.Supplier;

public interface Repository<K, V> extends ReadableRepository<K, V>, ListenableRepository<K, V> {

    V computeIfAbsent(K id, Supplier<V> supplier);

    void store(K id, V value);

    Collection<V> values();
}
