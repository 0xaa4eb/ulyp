package com.ulyp.core.metrics;

/**
 * Simple counter which must be thread-safe. Does not need to be 100% accurate
 */
public interface Counter {

    void add(long value);

    String getName();

    void dump(MetricOutput out);

    default void inc() {
        add(1);
    }
}
