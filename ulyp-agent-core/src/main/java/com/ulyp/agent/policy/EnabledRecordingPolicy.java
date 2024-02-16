package com.ulyp.agent.policy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnabledRecordingPolicy implements StartRecordingPolicy {

    @Override
    public boolean canStartRecording() {
        return true;
    }

    @Override
    public String toString() {
        return "Enabled";
    }
}
