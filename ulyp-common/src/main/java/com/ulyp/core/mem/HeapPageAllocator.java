package com.ulyp.core.mem;

import org.agrona.concurrent.UnsafeBuffer;

public class HeapPageAllocator implements PageAllocator {
    @Override
    public Page allocate() {
        return new Page(0, new UnsafeBuffer(new byte[Page.PAGE_BYTE_SIZE]));
    }

    @Override
    public void deallocate(Page page) {

    }

    @Override
    public void close() throws Exception {

    }
}
