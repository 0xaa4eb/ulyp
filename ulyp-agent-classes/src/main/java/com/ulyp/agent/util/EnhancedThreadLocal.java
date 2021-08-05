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

    public void computeIfAbsent(Supplier<T> newValueSupplier) {
        T value = tl.get();
        if (value == null) {
            tl.set(newValueSupplier.get());
        }
    }

    public T get() {
        return tl.get();
    }
}
