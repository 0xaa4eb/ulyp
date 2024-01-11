package com.ulyp.core.util;

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.Supplier;

public class ObjectPool<T> {

    public static final int MAX_TRIES_BEFORE_ALLOC = 3;

    private final Supplier<T> supplier;
    private final int indexMask;
    private final ObjectPoolClaim<T>[] objectArray; // object array is read only and not padded
    private final AtomicIntegerArray usedArray;

    public ObjectPool(int expectedThreads, Supplier<T> supplier) {
        if (Integer.bitCount(expectedThreads) != 1) {
            throw new IllegalArgumentException("Expected threads must be power of two, provided " + expectedThreads);
        }
        int entriesCount = expectedThreads * 4;
        this.indexMask = entriesCount - 1;
        this.supplier = supplier;
        //noinspection unchecked
        this.objectArray = (ObjectPoolClaim<T>[]) new ObjectPoolClaim[entriesCount];
        for (int i = 0; i < entriesCount; i++) {
            this.objectArray[i] = new ObjectPoolClaim(i, supplier.get());
        }

        this.usedArray = new AtomicIntegerArray(entriesCount * Constants.CACHE_LINE_INTS_COUNT);
    }

    ObjectPoolClaim<T> claim(int index, int tries) {
        int used = usedArray.get(index);
        if (used == 0) {
            if (usedArray.compareAndSet(index, 0, 1)) {
                return objectArray[index];
            }
        }

        if (tries < MAX_TRIES_BEFORE_ALLOC) {
            // it's taken, try next one
            return claim((index + 1) & indexMask, tries + 1);
        } else {
            // TODO maybe have some metrics
            return new ObjectPoolClaim<>(-1, supplier.get());
        }
    }

    private void release(int index) {
        usedArray.set(index, 0);
    }

    private int usedArrayIndex(int entryIndex) {
        return entryIndex * Constants.CACHE_LINE_INTS_COUNT;
    }

    public class ObjectPoolClaim<T> implements AutoCloseable {
        private final int index;
        private final T object;

        public ObjectPoolClaim(int index, T object) {
            this.index = index;
            this.object = object;
        }

        public T getObject() {
            return object;
        }

        @Override
        public void close() throws Exception {
            if (index >= 0) {
                release(index);
            }
        }
    }
}
