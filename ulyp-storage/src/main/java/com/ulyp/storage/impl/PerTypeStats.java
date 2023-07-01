package com.ulyp.storage.impl;

import java.util.concurrent.atomic.AtomicLong;

import com.ulyp.core.util.ByteSize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
// intentionally not thread safe, but call count is thread safe for tests only
public class PerTypeStats {

    private final String name;

    private ByteSize totalBytes = new ByteSize(0L);
    private AtomicLong totalCount = new AtomicLong(0L);

    public void reset() {

    }

    public long getTotalCount() {
        return totalCount.get();
    }

    public void addToCount(long delta) {
        totalCount.addAndGet(delta);
    }

    public void addBytes(long bytes) {
        totalBytes = totalBytes.addBytes(bytes);
    }

    @Override
    public String toString() {
        return "Recording stats for '" + name + "' total count: " + totalCount + ", total bytes: " + totalBytes;
    }
}
