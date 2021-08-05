package com.perf.agent.benchmarks;

import org.HdrHistogram.Histogram;

import java.io.Closeable;

public class MillisMeasured implements Closeable {

    private final Histogram histogram;
    private final long startTime;

    public MillisMeasured(Histogram histogram) {
        this.histogram = histogram;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void close() {
        histogram.recordValue(System.currentTimeMillis() - startTime);
    }
}
