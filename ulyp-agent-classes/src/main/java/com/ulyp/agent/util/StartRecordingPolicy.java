package com.ulyp.agent.util;

/**
 * Strategy to define when recording can start.
 */
public interface StartRecordingPolicy {

    /**
     * @return true if recording can start right now. Otherwise returns false
     */
    boolean canStartRecording();
}
