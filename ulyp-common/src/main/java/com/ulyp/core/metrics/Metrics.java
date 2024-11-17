package com.ulyp.core.metrics;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface Metrics {

    Counter getOrCreateCounter(String name);

    BytesCounter getOrCreateByteCounter(String name);

    void dump(MetricSink sink);
}
