package com.ulyp.storage;

import com.ulyp.storage.impl.RecordingState;

public class Recording {

    private final RecordingState recordingState;

    public Recording(RecordingState recordingState) {
        this.recordingState = recordingState;
    }

    public CallRecord getRoot() {
        return recordingState.getRoot();
    }
}
