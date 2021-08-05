package com.ulyp.agent.util;

import java.time.Duration;

/**
 * Strategy to define where recording can start
 */
public interface StartRecordingPolicy {

    static StartRecordingPolicy alwaysStartRecordingPolicy() {
        return new AlwaysStartRecordingPolicy();
    }

    static StartRecordingPolicy withDelayStartRecordingPolicy(Duration delay) {
        return new DelayBasedRecordingPolicy(delay);
    }

    boolean canStartRecording();
}
