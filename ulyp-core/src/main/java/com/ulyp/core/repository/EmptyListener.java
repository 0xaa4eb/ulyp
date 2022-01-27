package com.ulyp.core.repository;

public class EmptyListener<K, V> implements RepositoryListener<K, V> {

    @Override
    public void onNew(K key, V value) {

    }
}
