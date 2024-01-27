package com.ulyp.core.mem;

public interface PageAllocator extends AutoCloseable {

    Page allocate();

    void deallocate(Page page);
}
