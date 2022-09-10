package com.ulyp.agent.policy;

/**
 * Strategy to define when recording can start.
 */
public interface StartRecordingPolicy {

    /**
     * @return true if recording can start right now. Otherwise returns false
     */
    boolean canStartRecording();

    void forceEnableRecording(boolean recordingEnabled);
}
