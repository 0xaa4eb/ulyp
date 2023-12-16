package com.ulyp.agent.policy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DisabledRecordingPolicy implements StartRecordingPolicy {

    @Override
    public boolean canStartRecording() {
        return false;
    }

    @Override
    public String toString() {
        return "Disabled";
    }
}
