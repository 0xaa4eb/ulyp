package com.ulyp.core.repository;

public interface ListenableRepository<K, T> {

    void subscribe(RepositoryListener<K, T> listener);
}
