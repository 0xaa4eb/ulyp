package com.ulyp.core.mem;

import org.agrona.BitUtil;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class ManagedMemPool {

    public static final int PAGE_BITS = 14;
    public static final int PAGE_BYTE_SIZE = 1 << PAGE_BITS;
    public static final int PAGE_BYTE_SIZE_MASK = PAGE_BYTE_SIZE - 1;
    public static final int RECORDING_REGION_PAGE_COUNT = 128;

    private final ByteBuffer byteBuffer;
    private final UnsafeBuffer memory;
    private final Region[] regions;
    private final int regionCount;
    private final int regionCountMask;
    private final AtomicIntegerArray regionsUsed;

    public ManagedMemPool(int regionCount) {
        if (!BitUtil.isPowerOfTwo(regionCount)) {
            throw new RuntimeException("Recording pool must be power of two but was " + regionCount);
        }
        this.regions = new Region[regionCount];
        this.regionCount = regionCount;
        this.regionCountMask = regionCount - 1;
        this.regionsUsed = new AtomicIntegerArray(regionCount);
        int totalByteSize = regionCount * RECORDING_REGION_PAGE_COUNT * PAGE_BYTE_SIZE;
        this.byteBuffer = ByteBuffer.allocateDirect(totalByteSize);
        this.memory = new UnsafeBuffer(byteBuffer);
    }

    public Region allocate(int hint) {
        int regionIdToCheck = hint & regionCountMask;
        if (regionsUsed.get(regionIdToCheck) == 0 && regionsUsed.compareAndSet(regionIdToCheck, 0, 1)) {
            return regions[regionIdToCheck];
        }
        for (int i = 1; i < regionCount; i++) {
            int idx = (regionIdToCheck + 1) & regionCountMask;
            if (regionsUsed.get(idx) == 0 && regionsUsed.compareAndSet(idx, 0, 1)) {
                return regions[idx];
            }
        }
    }

    public void dispose() {

    }
}
