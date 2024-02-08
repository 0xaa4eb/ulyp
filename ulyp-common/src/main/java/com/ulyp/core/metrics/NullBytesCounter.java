package com.ulyp.core.metrics;

public class NullBytesCounter implements BytesCounter {

    public NullBytesCounter() {
    }

    @Override
    public void add(long value, int count) {
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void dump(MetricOutput out) {

    }
}
