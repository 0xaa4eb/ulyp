package com.ulyp.storage;

public interface ReadableRepository<T> {

    T get(long id);
}
