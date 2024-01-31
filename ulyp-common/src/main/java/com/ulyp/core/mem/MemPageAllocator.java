package com.ulyp.core.mem;

public interface MemPageAllocator {

    MemPage allocate();

    void deallocate(MemPage page);
}
