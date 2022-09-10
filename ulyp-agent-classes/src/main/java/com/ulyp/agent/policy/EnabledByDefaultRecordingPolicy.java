package com.ulyp.agent.policy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnabledByDefaultRecordingPolicy implements StartRecordingPolicy {

    private volatile boolean recordingEnabled = true;

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
