package com.ulyp.core.mem;

import org.agrona.BitUtil;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

public class ManagedMemPool {

    public static final int PAGE_BITS = 14;
    public static final int PAGE_BYTE_SIZE = 1 << PAGE_BITS;
    public static final int PAGE_BYTE_SIZE_MASK = PAGE_BYTE_SIZE - 1;
    public static final int RECORDING_REGION_PAGE_COUNT = 128;

    private final UnsafeBuffer memory;
    private final int regionCount;

    public ManagedMemPool(int regionCount) {
        if (!BitUtil.isPowerOfTwo(regionCount)) {
            throw new RuntimeException("Recording pool must be power of two but was " + regionCount);
        }
        this.regionCount = regionCount;
        int totalByteSize = regionCount * RECORDING_REGION_PAGE_COUNT * PAGE_BYTE_SIZE;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(totalByteSize);
        this.memory = new UnsafeBuffer(byteBuffer);
    }
}
