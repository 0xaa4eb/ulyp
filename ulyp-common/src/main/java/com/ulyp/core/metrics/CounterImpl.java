package com.ulyp.core.metrics;

import java.util.concurrent.atomic.LongAdder;

public class CounterImpl implements Counter {

    private final String name;
    private final LongAdder counter;

    public CounterImpl(String name) {
        this.name = name;
        this.counter = new LongAdder();
    }

    @Override
    public void add(long value) {
        counter.add(value);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getValue() {
        return counter.longValue();
    }

    @Override
    public void dump(MetricOutput out) {
        out.write("Counter ").write(name).write(" val: ").write(counter.longValue());
    }
}
