package com.ulyp.core.util;

import java.util.concurrent.locks.Lock;

public class LockGuard implements AutoCloseable {

    private final Lock lock;

    public LockGuard(Lock lock) {
        this.lock = lock;

        lock.lock();
    }

    @Override
    public void close() throws RuntimeException {
        lock.unlock();
    }
}
