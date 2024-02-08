package com.ulyp.core.metrics;

import com.ulyp.core.util.ByteSize;

import java.util.concurrent.atomic.LongAdder;

public class BytesCounterImpl implements BytesCounter {

    private final String name;
    private final LongAdder totalBytes;
    private final LongAdder count;

    public BytesCounterImpl(String name) {
        this.name = name;
        this.totalBytes = new LongAdder();
        this.count = new LongAdder();
    }

    @Override
    public void add(long value, int count) {
        this.count.add(count);
        this.totalBytes.add(value);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void dump(MetricOutput out) {
        out.write("Counter ");
        out.write(name);
        out.write(" val: ");
        out.write(ByteSize.toHumanReadable(totalBytes.longValue()));
        out.write(", count: ");
        out.write(count.longValue());
    }
}
