package com.ulyp.agent.util;

import java.util.function.Supplier;

public class EnhancedThreadLocal<T> {

    private final ThreadLocal<T> tl = new ThreadLocal<>();

    public EnhancedThreadLocal() {
        clear();
    }

    public void clear() {
        tl.set(null);
    }

    public void set(T v) {
        tl.set(v);
    }

    public T computeIfAbsent(Supplier<T> newValueSupplier) {
        T value = tl.get();
        if (value != null) {
            return value;
        } else {
            value = newValueSupplier.get();
            tl.set(value);
            return value;
        }
    }

    public T get() {
        return tl.get();
    }
}
