package com.ulyp.core;

public interface ReadableRepository<T> {

    T get(long id);
}
