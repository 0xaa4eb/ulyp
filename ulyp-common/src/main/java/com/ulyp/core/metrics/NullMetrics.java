package com.ulyp.core.metrics;

public class NullMetrics implements Metrics {

    public static final Metrics INSTANCE = new NullMetrics();

    @Override
    public Counter getOrCreateCounter(String name) {
        return new NullCounter();
    }

    @Override
    public BytesCounter getOrCreateByteCounter(String name) {
        return new NullBytesCounter();
    }

    @Override
    public void dump(MetricSink sink) {

    }
}
