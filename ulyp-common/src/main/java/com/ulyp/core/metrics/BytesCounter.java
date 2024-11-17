package com.ulyp.core.metrics;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Simple counter which must be thread-safe. Does not need to be 100% accurate
 */
@ThreadSafe
public interface BytesCounter {

    void add(long value, int count);

    String getName();

    void dump(MetricOutput out);
}
