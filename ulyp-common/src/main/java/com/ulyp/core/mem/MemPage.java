package com.ulyp.core.mem;

import com.ulyp.core.Resettable;
import lombok.Getter;
import lombok.Setter;
import org.agrona.concurrent.UnsafeBuffer;

@Getter
public class MemPage implements Resettable {
    private final int id;
    private final UnsafeBuffer buffer;
    @Setter
    private int unused;

    public MemPage(int id, UnsafeBuffer buffer) {
        this.id = id;
        this.buffer = buffer;
    }

    @Override
    public void reset() {
        unused = 0;
    }
}
