package com.ulyp.storage;

import com.ulyp.core.RecordingMetadata;
import com.ulyp.storage.impl.RecordingState;

import java.time.Duration;

public class Recording {

    private final RecordingState recordingState;

    public Recording(RecordingState recordingState) {
        this.recordingState = recordingState;
    }

    public int getId() {
        return recordingState.getId();
    }

    public CallRecord getRoot() {
        return recordingState.getRoot();
    }

    public int callCount() {
        return recordingState.callCount();
    }

    public RecordingMetadata getMetadata() {
        return recordingState.getMetadata();
    }

    public CallRecord getCallRecord(long callId) {
        return recordingState.getCallRecord(callId);
    }

    public Duration getLifetime() {
        return recordingState.getLifetime();
    }
}
