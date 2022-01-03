package com.ulyp.database;

public interface RepositoryReader<V> {

    V get(long id);
}
