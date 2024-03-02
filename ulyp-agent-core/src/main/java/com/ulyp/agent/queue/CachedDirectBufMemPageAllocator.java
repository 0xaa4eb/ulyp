package com.ulyp.agent.queue;

import com.ulyp.core.mem.MemPage;
import com.ulyp.core.mem.MemPageAllocator;
import com.ulyp.core.mem.PageConstants;
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
            return new DirectMemPage(0, new UnsafeBuffer(ByteBuffer.allocateDirect(PageConstants.PAGE_SIZE)));
        }
    }

    private class DirectMemPage extends MemPage {

        public DirectMemPage(int id, UnsafeBuffer buffer) {
            super(id, buffer);
        }

        @Override
        public void dispose() {
            super.dispose();

            deallocate(this);
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
