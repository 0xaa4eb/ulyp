package com.ulyp.core.util;

import com.ulyp.core.Resettable;

import java.util.function.Supplier;

public class FixedSizeObjectPool<T extends Resettable> {

    private final Supplier<T> supplier;
    private final Object[] entries;
    private int size = 0;

    public FixedSizeObjectPool(Supplier<T> supplier, int capacity) {
        this.entries = new Object[capacity];
        this.supplier = supplier;
        this.size = 0;
    }

    public T borrow() {
        if (size > 0) {
            @SuppressWarnings("unchecked")
            T toBorrow = (T) entries[size];
            size--;
            return toBorrow;
        } else {
            return supplier.get();
        }
    }

    public void requite(T item) {
        if (size < entries.length) {
            entries[size] = item;
            size++;
        }
    }

    protected int size() {
        return size;
    }
}
