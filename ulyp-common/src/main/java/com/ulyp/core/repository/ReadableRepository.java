package com.ulyp.core.repository;

public interface ReadableRepository<K, T> {

    T get(K id);
}
