package com.ulyp.core.mem;

import lombok.Getter;
import lombok.Setter;
import org.agrona.concurrent.UnsafeBuffer;

public class MemPage {
    @Getter
    private final int id;
    @Getter
    private final UnsafeBuffer buffer;
    @Getter @Setter
    private int unused; // TODO reset

    public MemPage(int id, UnsafeBuffer buffer) {
        this.id = id;
        this.buffer = buffer;
    }
}
