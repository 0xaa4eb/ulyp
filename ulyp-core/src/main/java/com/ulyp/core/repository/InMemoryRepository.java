package com.ulyp.core.repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class InMemoryRepository<K, V> implements Repository<K, V> {

    private final Map<K, V> map = new ConcurrentHashMap<>();
    private volatile RepositoryListener<K, V> listener = new EmptyListener<>();

    @Override
    public V get(K id) {
        return map.get(id);
    }

    @Override
    public V computeIfAbsent(K id, Supplier<V> supplier) {
        return map.computeIfAbsent(id, i -> {
            V newValue = supplier.get();
            if (newValue != null) {
                listener.onNew(id, newValue);
            }
            return newValue;
        });
    }

    @Override
    public void store(K id, V value) {
        map.compute(id, (i, existing) -> {
            if (existing == null) {
                listener.onNew(id, value);
            }
            return value;
        });
    }

    @Override
    public Collection<V> values() {
        return new ArrayList<>(map.values());
    }

    @Override
    public synchronized void subscribe(RepositoryListener<K, V> listener) {
        this.listener = new CompoundListener<>(Arrays.asList(this.listener, listener));
    }
}
