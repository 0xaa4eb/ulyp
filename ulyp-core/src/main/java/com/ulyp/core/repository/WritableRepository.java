package com.ulyp.core.repository;

public interface WritableRepository<K, V> {

    void store(K id, V value);
}
