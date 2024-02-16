package com.ulyp.core.mem;

import lombok.Getter;
import org.agrona.concurrent.UnsafeBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

// !!! not used
/**
 * Single allocator - single deallocator (SASD) concurrency is supported. At any point of time, only a single thread
 * may borrow a memory region for allocating purposes. At the same time pages might be used for sending data
 * to a different thread which deallocates pages. The other thread should also return mem region to the pool after recording is
 * complete.
 */
public class MemRegion {

    private final int pagesCount;
    private final int pagesCountMask;
    @Getter
    private final UnsafeBuffer buffer;
    private final List<MemPage> pages;
    private final AtomicIntegerArray usedPages; // this adds some false sharing but should be rarely accessed
    private final AtomicInteger lastBorrowedPageId = new AtomicInteger(0);
    private volatile State state;

    public MemRegion(UnsafeBuffer buffer, int pageSize, int pagesCount) {
        this.buffer = buffer;
        this.pagesCount = pagesCount;
        this.usedPages = new AtomicIntegerArray(pagesCount);
        this.pages = new ArrayList<>(pagesCount);
        this.pagesCountMask = pagesCount - 1;
        for (int pgIndex = 0; pgIndex < pagesCount; pgIndex++) {
            UnsafeBuffer pageBuffer = new UnsafeBuffer();
            pageBuffer.wrap(buffer, pgIndex * pageSize, pageSize);
            this.pages.add(new MemPage(pgIndex, pageBuffer));
        }
    }

    enum State {
        BORROWED,
        FREE
    }

    public MemPage allocate() {
        int checkPageId = (lastBorrowedPageId.get() + 1) & pagesCountMask;
        int used = usedPages.get(checkPageId);
        if (used == 0) {
            usedPages.lazySet(checkPageId, 1); // single allocator, no CAS is required
            MemPage page = pages.get(checkPageId);
            page.reset();
            return page;
        }
        // the next page is not returned yet, try some next
        for (int i = 0; i < pagesCount; i++) {
            int iShifted = (checkPageId + i) & pagesCountMask;
            if (usedPages.get(iShifted) == 0) {
                usedPages.lazySet(iShifted, 1);
                MemPage page = pages.get(iShifted);
                page.reset();
                return page;
            }
        }
        return null;
    }

    public void deallocate(MemPage page) {
        usedPages.lazySet(page.getId(), 0);
    }
}
