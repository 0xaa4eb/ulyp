package com.ulyp.core.repository;

import java.util.ArrayList;
import java.util.List;

public class CompoundListener<K, V> implements RepositoryListener<K, V> {

    private final List<RepositoryListener<K, V>> listeners;

    public CompoundListener(List<RepositoryListener<K, V>> listeners) {
        this.listeners = new ArrayList<>(listeners);
    }

    @Override
    public void onNew(K key, V value) {
        for (RepositoryListener<K, V> listener : listeners) {
            listener.onNew(key, value);
        }
    }
}
