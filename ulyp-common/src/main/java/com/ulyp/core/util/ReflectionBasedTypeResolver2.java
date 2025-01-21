package com.ulyp.core.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.jetbrains.annotations.NotNull;

import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReflectionBasedTypeResolver2 implements TypeResolver {

    private final int capacity;
    private final AtomicReferenceArray<Class<?>> keys;
    private final AtomicReferenceArray<Type> values;
    private int mask;
    private final AtomicInteger idGen = new AtomicInteger();
    private final ConcurrentArrayList<Type> typesList = new ConcurrentArrayList<>();

    public ReflectionBasedTypeResolver2(int capacity) {
        this.capacity = capacity;
        this.keys = new AtomicReferenceArray<>(capacity);
        this.values = new AtomicReferenceArray<>(capacity);
        this.mask = capacity - 1;
    }

    private Type build(Class<?> clazz) {
        Type.TypeBuilder type = Type.builder();
        type.name(clazz.getName());
        type.id(idGen.incrementAndGet());
        return type.build();
    }

    @Override
    public @NotNull Type get(Object o) {
        if (o != null) {
            return get(o.getClass());
        } else {
            return Type.unknown();
        }
    }

    @Override
    public @NotNull Type get(Class<?> clazz) {
        Type type = getImpl(clazz);
        if (type != null) {
            return type;
        }

        Type resolved = build(clazz);
        typesList.add(resolved);
        putImpl(clazz, resolved);
        return resolved;
    }

    private Type getImpl(Class<?> key) {
        int hashCode = key.hashCode();
        int bucketId = hashCode & mask;

        Class<?> keyAtBucket = keys.get(bucketId);
        if (keyAtBucket == key) {
            return values.get(bucketId);
        }
        if (keyAtBucket == null) {
            return null;
        }
        for (int i = 1;; i++) {
            bucketId = (bucketId + 1) & mask;
            keyAtBucket = keys.get(bucketId);
            if (keyAtBucket == key) {
                return values.get(bucketId);
            }
            if (keyAtBucket == null) {
                return null;
            }
        }
    }

    private void putImpl(Class<?> key, Type value) {
        int hashCode = key.hashCode();
        int bucketId = hashCode & mask;
        Class<?> keyAtBucket = keys.get(bucketId);
        if (keyAtBucket == null && keys.compareAndSet(bucketId, null, key)) {
            values.set(bucketId, value);
        }
        if (keyAtBucket == key) {
            values.set(bucketId, value);
        }
        for (int i = 1;; i++) {
            bucketId = (bucketId + 1) & mask;
            keyAtBucket = keys.get(bucketId);
            if (keyAtBucket == null && keys.compareAndSet(bucketId, null, key)) {
                values.set(bucketId, value);
                return;
            }
        }
    }

    @Override
    public @NotNull ConcurrentArrayList<Type> values() {
        return typesList;
    }
}
