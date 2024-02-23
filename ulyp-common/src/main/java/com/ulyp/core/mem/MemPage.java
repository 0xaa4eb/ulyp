package com.ulyp.core.mem;

import lombok.Getter;
import lombok.Setter;
import org.agrona.concurrent.UnsafeBuffer;

@Getter
public class MemPage {
    private final int id;
    private final UnsafeBuffer buffer;
    @Setter
    private int unused;

    public MemPage(int id, UnsafeBuffer buffer) {
        this.id = id;
        this.buffer = buffer;
    }

    public void reset() {
        unused = 0;
    }

    public void dispose() {
    }
}
