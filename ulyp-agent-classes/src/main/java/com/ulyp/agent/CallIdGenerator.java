package com.ulyp.agent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Every method recorded has call id which is unique across all recordings for a certain
 * recording file. In order to avoid contention between threads hi-lo algorithm is used,
 * so any recording just gets some initial value and then locally increments it to generate
 * next call id.
 */
public class CallIdGenerator {

    public static final long MAX_CALLS_PER_RECORD_LOG = 100_000_000;

    private final AtomicLong current = new AtomicLong(-MAX_CALLS_PER_RECORD_LOG);

    public long getNextStartValue() {
        return current.addAndGet(MAX_CALLS_PER_RECORD_LOG);
    }
}
