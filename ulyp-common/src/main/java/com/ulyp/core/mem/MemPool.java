package com.ulyp.core.mem;

import org.agrona.BitUtil;
import org.agrona.concurrent.UnsafeBuffer;
import sun.jvm.hotspot.memory.MemRegion;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MemPool {

    public static final int PAGE_BITS = 15;
    public static final int PAGE_SIZE = 1 << PAGE_BITS;
    public static final int PAGE_BYTE_SIZE_MASK = PAGE_SIZE - 1;
    public static final int RECORDING_REGION_PAGE_COUNT = 128;

    private final ByteBuffer byteBuffer;
    private final UnsafeBuffer memory;
    private final Deque<MemRegion> regions;

    public MemPool(int regionCount) {
        if (!BitUtil.isPowerOfTwo(regionCount)) {
            throw new RuntimeException("Recording pool must be power of two but was " + regionCount);
        }
        this.regions = new ConcurrentLinkedDeque<>();
        int totalByteSize = regionCount * RECORDING_REGION_PAGE_COUNT * PAGE_SIZE;
        this.byteBuffer = ByteBuffer.allocateDirect(totalByteSize);
        this.memory = new UnsafeBuffer(byteBuffer);
    }

    public MemRegion allocate() {
        MemRegion region = regions.removeLast();
        if (region != null) {
            // TODO change state
            return region;
        } else {
            return null;
        }
    }

    public void dispose(MemRegion region) {
        regions.addLast(region);
    }
}
