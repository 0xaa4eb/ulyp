package com.ulyp.storage;

import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.recorders.NotRecordedObjectRecord;
import com.ulyp.storage.impl.RecordingState;

import java.time.Duration;

import javax.annotation.Nonnull;

/**
* Recording class represents a single recorded method call along with its child subcalls.
*/
public class Recording {

    private final RecordingState recordingState;

    public Recording(RecordingState recordingState) {
        this.recordingState = recordingState;
    }

    public int getId() {
        return recordingState.getId();
    }

    @Nonnull
    public CallRecord getRoot() {
        return recordingState.getRoot();
    }

    public boolean isComplete() {
        return !(getRoot().getReturnValue() instanceof NotRecordedObjectRecord);
    }

    public int callCount() {
        return recordingState.callCount();
    }

    @Nonnull
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
