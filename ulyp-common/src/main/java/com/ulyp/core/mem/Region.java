package com.ulyp.core.mem;

import org.agrona.concurrent.UnsafeBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Region {

    private final int id;
    private final int pagesCountMask;
    private final UnsafeBuffer memory;
    private final List<Page> pages;
    private final AtomicInteger lastBorrowedPageId = new AtomicInteger(0);

    public Region(int id, UnsafeBuffer memory, int pageSize, int pagesCount) {
        this.memory = memory;
        this.pages = new ArrayList<>(pagesCount);
        this.pagesCountMask = pagesCount - 1;
        this.id = id;
        for (int i = 0; i < pagesCount; i++) {
            UnsafeBuffer unsafeBuffer = new UnsafeBuffer();
            memory.getBytes(i * pageSize, unsafeBuffer, 0, pageSize);
            this.pages.add(new Page(i, id, unsafeBuffer));
        }
    }
}
