package com.ulyp.core.util;

import java.time.Duration;

public class FixedDelayBackoff implements Backoff {

    private final Duration backoffDuration;

    public FixedDelayBackoff(Duration backoffDuration) {
        this.backoffDuration = backoffDuration;
    }

    @Override
    public void await() throws InterruptedException {
        Thread.sleep(backoffDuration.toMillis());
    }
}
