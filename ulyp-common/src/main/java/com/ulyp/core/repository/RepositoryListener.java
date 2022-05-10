package com.ulyp.core.repository;

public interface RepositoryListener<K, V> {

    void onNew(K key, V value);
}
