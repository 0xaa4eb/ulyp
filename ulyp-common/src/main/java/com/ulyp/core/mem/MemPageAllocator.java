package com.ulyp.core.mem;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface MemPageAllocator extends AutoCloseable {

    MemPage allocate();

    void deallocate(MemPage page);

    @Override
    default void close() throws RuntimeException {}
}
