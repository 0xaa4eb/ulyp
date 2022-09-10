package com.ulyp.agent.policy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisabledByDefaultRecordingPolicy implements StartRecordingPolicy {

    private volatile boolean recordingEnabled = false;

    @Override
    public boolean canStartRecording() {
        return recordingEnabled;
    }

    @Override
    public void forceEnableRecording(boolean recordingEnabled) {
        this.recordingEnabled = recordingEnabled;
        log.info("Recording enabled set to {}", recordingEnabled);
    }

    @Override
    public String toString() {
        return "Can start recording any time";
    }
}
