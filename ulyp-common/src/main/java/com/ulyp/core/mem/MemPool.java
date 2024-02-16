package com.ulyp.core.mem;

import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class MemPool {

    public static final int PAGE_BITS = 15;
    public static final int PAGE_SIZE = 1 << PAGE_BITS;
    public static final int PAGE_BYTE_SIZE_MASK = PAGE_SIZE - 1;
    public static final int REGION_PAGE_COUNT = 128;
    public static final int REGION_BYTE_SIZE = REGION_PAGE_COUNT * PAGE_SIZE;
    public static final int REGION_COUNT = 64;

    private final Deque<MemRegion> regions;
    private final AtomicInteger regionsStored = new AtomicInteger();

    public MemPool() {
        this.regions = new ConcurrentLinkedDeque<>();
    }

    public MemRegion allocate() {
        MemRegion region = regions.pollLast();
        if (region != null) {
            // TODO change state
            return region;
        } else {
            return new MemRegion(new UnsafeBuffer(ByteBuffer.allocateDirect(REGION_BYTE_SIZE)), PAGE_SIZE, REGION_PAGE_COUNT);
        }
    }

    public void dispose(MemRegion region) {
        if (regionsStored.get() < REGION_COUNT) {
            regions.addLast(region);
            regionsStored.incrementAndGet();
        }
    }
}
