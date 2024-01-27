package com.ulyp.core.mem;

import org.agrona.concurrent.UnsafeBuffer;

public class Page {

    private final int id;
    private final int regionId;
    private final UnsafeBuffer buffer;

    public Page(int id, int regionId, UnsafeBuffer buffer) {
        this.id = id;
        this.regionId = regionId;
        this.buffer = buffer;
    }

    public int getId() {
        return id;
    }

    public int getRegionId() {
        return regionId;
    }

    public UnsafeBuffer getBuffer() {
        return buffer;
    }
}
