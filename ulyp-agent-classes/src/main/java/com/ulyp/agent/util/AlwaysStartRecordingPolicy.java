package com.ulyp.agent.util;

public class AlwaysStartRecordingPolicy implements StartRecordingPolicy {

    @Override
    public boolean canStartRecording() {
        return true;
    }

    @Override
    public String toString() {
        return "Can start recording any time";
    }
}
