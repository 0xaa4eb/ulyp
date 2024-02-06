package com.ulyp.core.mem;

public interface MemPageAllocator extends AutoCloseable {

    MemPage allocate();

    void deallocate(MemPage page);

    @Override
    default void close() throws RuntimeException {}
}
