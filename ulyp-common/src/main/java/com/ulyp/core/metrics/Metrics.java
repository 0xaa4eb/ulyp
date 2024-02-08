package com.ulyp.core.metrics;

public interface Metrics {

    Counter getOrCreateCounter(String name);

    BytesCounter getOrCreateByteCounter(String name);

    void dump(MetricSink sink);
}
