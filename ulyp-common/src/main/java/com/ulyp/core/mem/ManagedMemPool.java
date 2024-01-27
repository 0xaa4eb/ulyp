package com.ulyp.core.mem;

import org.agrona.BitUtil;
import org.agrona.concurrent.UnsafeBuffer;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ManagedMemPool {

    private final Queue<Region> regions;

    public ManagedMemPool(UnsafeBuffer buffer, int regionCount, int pagesPerRegion) {
        if (!BitUtil.isPowerOfTwo(regionCount)) {
            throw new RuntimeException("Recording pool must be power of two but was " + regionCount);
        }
        if (!BitUtil.isPowerOfTwo(buffer.capacity())) {
            throw new RuntimeException("Buffer capacity must be power of two but was " + regionCount);
        }
        this.regions = new ConcurrentLinkedQueue<>();
        int regionSize = buffer.capacity() / regionCount;
        for (int i = 0; i < regionCount; i++) {
            UnsafeBuffer regionBuffer = new UnsafeBuffer();
            regionBuffer.wrap(buffer, regionSize * i, regionSize);
            regions.add(new Region(i, regionBuffer, pagesPerRegion));
        }
    }

    public Region borrow() {
        return regions.poll();
    }

    public void requite(Region region) {
        regions.add(region);
    }
}
