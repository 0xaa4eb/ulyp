package com.ulyp.core.mem;

import org.agrona.concurrent.UnsafeBuffer;

public class Page {

    public static final int PAGE_BITS = 15;
    public static final int PAGE_BYTE_SIZE = 1 << PAGE_BITS;
    public static final int PAGE_BYTE_SIZE_MASK = PAGE_BYTE_SIZE - 1;

    private final int id;
    private final UnsafeBuffer buffer;

    public Page(int id, UnsafeBuffer buffer) {
        this.id = id;
        this.buffer = buffer;
        if (buffer.capacity() != PAGE_BYTE_SIZE) {
            throw new IllegalArgumentException("Buffer capacity is invalid, only " + PAGE_BYTE_SIZE + " is supported");
        }
    }

    public int getId() {
        return id;
    }

    public UnsafeBuffer getBuffer() {
        return buffer;
    }
}
