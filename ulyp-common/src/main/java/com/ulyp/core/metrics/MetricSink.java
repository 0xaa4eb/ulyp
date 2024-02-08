package com.ulyp.core.metrics;

public interface MetricSink {

    void dump(Counter counter);

    void dump(BytesCounter bytesCounter);
}
