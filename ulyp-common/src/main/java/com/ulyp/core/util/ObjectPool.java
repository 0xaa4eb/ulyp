package com.ulyp.core.util;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class ObjectPool<T> {

    public static final int MAX_TRIES_BEFORE_ALLOC = 3;

    private final Supplier<T> supplier;
    private final int indexMask;
    private final ObjectPoolClaim<T>[] objectArray; // object array is read only and not padded
    private final PaddedAtomicIntegerArray used;

    public ObjectPool(int entriesCount, Supplier<T> supplier) {
        if (Integer.bitCount(entriesCount) != 1) {
            throw new IllegalArgumentException("Entry count must be power of two, but was " + entriesCount);
        }
        this.indexMask = entriesCount - 1;
        this.supplier = supplier;
        //noinspection unchecked
        this.objectArray = (ObjectPoolClaim<T>[]) new ObjectPoolClaim[entriesCount];
        for (int i = 0; i < entriesCount; i++) {
            //noinspection resource
            this.objectArray[i] = new ObjectPoolClaim<>(this, i, supplier.get());
        }

        this.used = new PaddedAtomicIntegerArray(entriesCount);
    }

    public ObjectPoolClaim<T> claim() {
        int id = ThreadLocalRandom.current().nextInt();
        return claim(id & indexMask, 0);
    }

    private ObjectPoolClaim<T> claim(int index, int tries) {
        int used = this.used.get(index);
        if (used == 0) {
            if (this.used.compareAndSet(index, 0, 1)) {
                return objectArray[index];
            }
        }

        if (tries < MAX_TRIES_BEFORE_ALLOC) {
            // it's taken, try next one
            return claim((index + 1) & indexMask, tries + 1);
        } else {
            // TODO maybe have some metrics
            return new ObjectPoolClaim<>(this, -1, supplier.get());
        }
    }

    private void release(int index) {
        used.set(index, 0);
    }

    public static class ObjectPoolClaim<T> implements AutoCloseable {
        private final ObjectPool<T> pool;
        private final int index;
        private final T object;

        public ObjectPoolClaim(ObjectPool<T> pool, int index, T object) {
            this.pool = pool;
            this.index = index;
            this.object = object;
        }

        public T get() {
            return object;
        }

        @Override
        public void close() throws RuntimeException {
            if (index >= 0) {
                pool.release(index);
            }
        }
    }
}