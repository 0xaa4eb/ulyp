package com.ulyp.agent.queue;

import com.ulyp.core.mem.MemPage;
import com.ulyp.core.mem.MemPageAllocator;
import com.ulyp.core.mem.MemPool;
import com.ulyp.core.mem.MemRegion;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

// not used
public class RecordingMemPageAllocator implements MemPageAllocator {

    private final MemPool memPool;
    private final List<MemRegion> regions = new ArrayList<>();
    private int lastUsedRegion = 0;

    public RecordingMemPageAllocator(MemPool memPool) {
        this.memPool = memPool;
        allocateRegion();
    }

    private MemRegion allocateRegion() {
        MemRegion region = memPool.allocate();
        regions.add(region);
        return region;
    }

    @Override
    public MemPage allocate() {
        MemRegion region = regions.get(lastUsedRegion);
        MemPage page = region.allocate();
        if (page != null) {
            return page;
        }
        for (MemRegion r : regions) {
            page = r.allocate();
            if (page != null) {
                return page;
            }
        }
        MemRegion newRegion = allocateRegion();
        if (newRegion != null) {
            return newRegion.allocate();
        } else {
            return new MemPage(0, new UnsafeBuffer(ByteBuffer.allocateDirect(MemPool.PAGE_SIZE)));
        }
    }

    @Override
    public void deallocate(MemPage page) {
        page.dispose();
    }

    @Override
    public void close() throws RuntimeException {
        for (MemRegion region : regions) {
            memPool.dispose(region);
        }
    }
}
