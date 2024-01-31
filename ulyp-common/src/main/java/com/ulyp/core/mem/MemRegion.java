package com.ulyp.core.mem;

import org.agrona.concurrent.UnsafeBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Single allocator - single deallocator (SASD) concurrency is supported. At any point of time, only a single thread
 * may borrow a memory region for allocating purposes. At the same time pages might be used for sending data
 * to a different thread which deallocates pages. The other thread should also return mem region to the pool after recording is
 * complete.
 */
public class MemRegion {

    private final int id;
    private final int pagesCount;
    private final int pagesCountMask;
    private final UnsafeBuffer buffer;
    private final List<MemPage> pages;
    private final AtomicIntegerArray usedPages; // this adds some false sharing but should be rarely accessed
    private final AtomicInteger lastBorrowedPageId = new AtomicInteger(0);

    public MemRegion(int id, UnsafeBuffer buffer, int pageSize, int pagesCount) {
        this.buffer = buffer;
        this.pagesCount = pagesCount;
        this.usedPages = new AtomicIntegerArray(pagesCount);
        this.pages = new ArrayList<>(pagesCount);
        this.pagesCountMask = pagesCount - 1;
        this.id = id;
        for (int pgIndex = 0; pgIndex < pagesCount; pgIndex++) {
            UnsafeBuffer unsafeBuffer = new UnsafeBuffer();
            buffer.getBytes(pgIndex * pageSize, unsafeBuffer, 0, pageSize);
            this.pages.add(new MemPage(pgIndex, unsafeBuffer));
        }
    }

    public MemPage allocate() {
        int checkPageId = (lastBorrowedPageId.get() + 1) & pagesCountMask;
        int used = usedPages.get(checkPageId);
        if (used == 0) {
            usedPages.lazySet(checkPageId, 1); // single allocator, no CAS is required
            return pages.get(checkPageId);
        }
        // the next page is not returned yet, try some next
        for (int i = 0; i < pagesCount; i++) {
            int iShifted = (checkPageId + i) & pagesCountMask;
            if (usedPages.get(iShifted) == 0) {
                usedPages.lazySet(iShifted, 1);
                return pages.get(iShifted);
            }
        }
        return null;
    }

    public void deallocate(MemPage page) {
        usedPages.lazySet(page.getId(), 0);
    }

    public UnsafeBuffer getBuffer() {
        return buffer;
    }
}
