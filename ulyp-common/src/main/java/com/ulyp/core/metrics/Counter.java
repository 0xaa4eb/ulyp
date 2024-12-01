package com.ulyp.core.metrics;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Simple counter which must be thread-safe. Does not need to be 100% accurate
 */
@ThreadSafe
public interface Counter {

    void add(long value);

    String getName();

    long getValue();

    void dump(MetricOutput out);

    default void inc() {
        add(1);
    }
}
