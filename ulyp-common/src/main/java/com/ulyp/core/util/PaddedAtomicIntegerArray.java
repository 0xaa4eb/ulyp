package com.ulyp.core.util;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class PaddedAtomicIntegerArray {

    private final AtomicIntegerArray array;

    public PaddedAtomicIntegerArray(int capacity) {
        if (Integer.bitCount(capacity) != 1) {
            throw new IllegalArgumentException("Expected threads must be power of two, provided " + capacity);
        }
        this.array = new AtomicIntegerArray(toInternalIndex(capacity));
    }

    public int get(int index) {
        return this.array.get(toInternalIndex(index));
    }

    public void set(int index, int value) {
        this.array.set(toInternalIndex(index), value);
    }

    public boolean compareAndSet(int index, int expected, int value) {
        return this.array.compareAndSet(toInternalIndex(index), expected, value);
    }

    private int toInternalIndex(int index) {
        return index << Constants.CACHE_LINE_INTS_BITS_SHIFT;
    }
}
