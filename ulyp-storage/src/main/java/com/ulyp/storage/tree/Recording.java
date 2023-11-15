package com.ulyp.storage.tree;

import com.ulyp.core.RecordingMetadata;
import org.jetbrains.annotations.NotNull;
import java.time.Duration;

/**
* Recording class represents a particular recorded method call along with all its child subcalls. There are
* usually many recordings present.
*/
public class Recording {

    private final RecordingState recordingState;

    public Recording(RecordingState recordingState) {
        this.recordingState = recordingState;
    }

    public int getId() {
        return recordingState.getId();
    }

    @NotNull
    public CallRecord getRoot() {
        return recordingState.getRoot();
    }

    public int callCount() {
        return recordingState.callCount();
    }

    @NotNull
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
