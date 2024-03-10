package com.ulyp.core.util;

import java.util.function.Supplier;

/**
 * Simplest object pool. The primary use case is short-term borrow and return pattern where same
 * thread ideally
 */
public class ConcurrentSimpleObjectPool<T> {

    public static final int MAX_TRIES_BEFORE_ALLOC = 3;

    private final Supplier<T> supplier;
    private final int indexMask;
    private final ObjectPoolClaim<T>[] objectArray; // object array is read only and not padded
    private final PaddedAtomicIntegerArray slotsUsed;

    public ConcurrentSimpleObjectPool(int entriesCount, Supplier<T> supplier) {
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

        this.slotsUsed = new PaddedAtomicIntegerArray(entriesCount);
    }

    public ObjectPoolClaim<T> claim() {
        long id = Thread.currentThread().getId();
        return claim(((int) id) & indexMask, 0);
    }

    private ObjectPoolClaim<T> claim(int index, int tries) {
        int slotUsed = this.slotsUsed.get(index);
        if (slotUsed == 0 && this.slotsUsed.compareAndSet(index, 0, 1)) {
            return objectArray[index];
        }

        if (tries < MAX_TRIES_BEFORE_ALLOC) {
            // it's taken, try next one
            return claim((index + 1) & indexMask, tries + 1);
        } else {
            // TODO maybe have some metrics
            return new ObjectPoolClaim<>(this, -1, supplier.get());
        }
    }

    private void release(int slotIndex) {
        slotsUsed.set(slotIndex, 0);
    }

    public static class ObjectPoolClaim<T> implements AutoCloseable {
        private final ConcurrentSimpleObjectPool<T> pool;
        private final int slotIndex;
        private final T object;

        public ObjectPoolClaim(ConcurrentSimpleObjectPool<T> pool, int slotIndex, T object) {
            this.pool = pool;
            this.slotIndex = slotIndex;
            this.object = object;
        }

        public T get() {
            return object;
        }

        @Override
        public void close() throws RuntimeException {
            if (slotIndex >= 0) {
                pool.release(slotIndex);
            }
        }
    }
}
