package com.ulyp.agent.queue;

import com.ulyp.core.mem.MemPage;
import com.ulyp.core.mem.MemPageAllocator;
import com.ulyp.core.mem.MemPool;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;

public class DirectBufMemPageAllocator implements MemPageAllocator {

    @Override
    public MemPage allocate() {
        return new MemPage(0, new UnsafeBuffer(ByteBuffer.allocateDirect(MemPool.PAGE_SIZE)));
    }


    @Override
    public void deallocate(MemPage page) {

    }

    @Override
    public void close() throws RuntimeException {

    }
}
