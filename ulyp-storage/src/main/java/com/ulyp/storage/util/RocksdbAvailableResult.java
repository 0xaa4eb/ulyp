package com.ulyp.storage.util;

public class RocksdbAvailableResult {

    private final boolean value;
    private final Throwable err;

    public RocksdbAvailableResult(boolean value) {
        this(value, null);
    }

    public RocksdbAvailableResult(boolean value, Throwable err) {
        this.value = value;
        this.err = err;
    }

    public boolean value() {
        return value;
    }

    public Throwable getErr() {
        return err;
    }
}
