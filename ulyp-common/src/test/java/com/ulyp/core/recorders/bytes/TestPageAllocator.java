package com.ulyp.core.recorders.bytes;

import com.ulyp.core.mem.MemPage;
import com.ulyp.core.mem.MemPageAllocator;
import com.ulyp.core.mem.PageConstants;
import lombok.Getter;
import org.agrona.concurrent.UnsafeBuffer;

import java.util.ArrayList;
import java.util.List;

public class TestPageAllocator implements MemPageAllocator {

    @Getter
    private final List<MemPage> pages = new ArrayList<>();

    @Override
    public MemPage allocate() {
        MemPage memPage = new MemPage(0, new UnsafeBuffer(new byte[PageConstants.PAGE_SIZE]));
        pages.add(memPage);
        return memPage;
    }

    @Override
    public void deallocate(MemPage page) {
        pages.remove(page);
    }
}
