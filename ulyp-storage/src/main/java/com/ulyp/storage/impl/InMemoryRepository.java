package com.ulyp.storage.impl;

import com.ulyp.storage.Repository;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

public class InMemoryRepository<T> implements Repository<T> {

    private final Long2ObjectMap<T> map = new Long2ObjectOpenHashMap<>();

    @Override
    public synchronized T get(long id) {
        return map.get(id);
    }

    @Override
    public T computeIfAbsent(long id, Supplier<T> supplier) {
        return map.computeIfAbsent(id, i -> supplier.get());
    }

    @Override
    public synchronized void store(long id, T value) {
        map.put(id, value);
    }

    @Override
    public Collection<T> values() {
        return new ArrayList<>(map.values());
    }
}
