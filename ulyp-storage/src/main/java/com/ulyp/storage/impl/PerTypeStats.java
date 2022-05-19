package com.ulyp.storage.impl;

import com.ulyp.core.util.ByteSize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PerTypeStats {

    private final String name;

    private ByteSize totalBytes = new ByteSize(0L);
    private long totalCount = 0L;

    public void addToCount(long delta) {
        totalCount += delta;
    }

    public void addBytes(long bytes) {
        totalBytes = totalBytes.addBytes(bytes);
    }

    @Override
    public String toString() {
        return "Recording stats for '" + name + "' total count: " + totalCount + ", total bytes: " + totalBytes;
    }
}
