package com.ulyp.core.mem;

import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class CachedDirectBufMemPageAllocator implements MemPageAllocator {

    private static final int CACHE_PAGES_COUNT = 2048;

    private final Deque<MemPage> pages = new ConcurrentLinkedDeque<>();
    private final AtomicInteger pagesCached = new AtomicInteger(0);

    @Override
    public MemPage allocate() {
        MemPage memPage = pages.pollLast();
        if (memPage != null) {
            pagesCached.decrementAndGet();
            memPage.reset();
            return memPage;
        } else {
            return new MemPage(0, new UnsafeBuffer(ByteBuffer.allocateDirect(PageConstants.PAGE_SIZE)));
        }
    }

    @Override
    public void deallocate(MemPage page) {
        if (pagesCached.get() < CACHE_PAGES_COUNT) {
            pages.addLast(page);
            pagesCached.incrementAndGet();
        }
    }
}
