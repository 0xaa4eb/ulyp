package com.ulyp.storage.util;

public class RocksdbAvailableResult {

    private final boolean available;
    private final Throwable err;

    public RocksdbAvailableResult(boolean available) {
        this(available, null);
    }

    public RocksdbAvailableResult(boolean available, Throwable err) {
        this.available = available;
        this.err = err;
    }

    public boolean isAvailable() {
        return available;
    }

    public Throwable getErr() {
        return err;
    }
}
