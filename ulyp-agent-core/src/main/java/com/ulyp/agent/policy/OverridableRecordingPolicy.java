package com.ulyp.agent.policy;

public class OverridableRecordingPolicy implements StartRecordingPolicy {

    private final StartRecordingPolicy delegate;
    private volatile Boolean recordingCanStart = null;

    public OverridableRecordingPolicy(StartRecordingPolicy delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean canStartRecording() {
        return recordingCanStart != null ? recordingCanStart : delegate.canStartRecording();
    }

    public void setRecordingCanStart(boolean recordingEnabled) {
        recordingCanStart = recordingEnabled ? true : null;
    }
}
