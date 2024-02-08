package com.ulyp.core.metrics;

public class NullCounter implements Counter {

    @Override
    public void add(long value) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void dump(MetricOutput out) {

    }
}
