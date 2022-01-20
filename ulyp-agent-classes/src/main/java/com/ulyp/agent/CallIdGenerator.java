package com.ulyp.agent;

import java.util.concurrent.atomic.AtomicLong;


public class CallIdGenerator {

    private static final long MAX_CALLS_PER_RECORD_LOG = 100_000_000;

    private final AtomicLong current = new AtomicLong(-MAX_CALLS_PER_RECORD_LOG);

    public long generateCallId() {
        return current.addAndGet(MAX_CALLS_PER_RECORD_LOG);
    }
}
