package com.ulyp.storage.writer;

import com.ulyp.core.util.ByteSize;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
// intentionally not thread safe, but call count is thread safe for tests only
public class PerTypeStats {

    private final String name;

    private ByteSize totalBytes = new ByteSize(0L);
    private final AtomicLong totalCount = new AtomicLong(0L);

    public void reset() {

    }

    public long getTotalBytes() {
        return totalBytes.getByteSize();
    }

    public void addBytes(long bytes) {
        totalBytes = totalBytes.addBytes(bytes);
    }

    @Override
    public String toString() {
        return "Recording stats for '" + name + "' total count: " + totalCount + ", total bytes: " + totalBytes;
    }
}
