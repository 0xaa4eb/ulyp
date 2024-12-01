package com.ulyp.core.repository;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@ThreadSafe
public class InMemoryRepository<K, V> implements Repository<K, V> {

    private final Map<K, V> map = new ConcurrentHashMap<>();

    @Override
    public V get(K id) {
        return map.get(id);
    }

    public V computeIfAbsent(K id, Supplier<V> supplier) {
        return map.computeIfAbsent(id, i -> supplier.get());
    }

    @Override
    public void store(K id, V value) {
        map.put(id, value);
    }

    public Collection<V> values() {
        return new ArrayList<>(map.values());
    }
}
