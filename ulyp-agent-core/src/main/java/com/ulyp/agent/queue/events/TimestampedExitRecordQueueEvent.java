package com.ulyp.agent.queue.events;

import lombok.Getter;

@Getter
public class TimestampedExitRecordQueueEvent extends ExitRecordQueueEvent {

    private long nanoTime;

    public void set(int recordingId, int callId, Object returnValue, boolean thrown, long nanoTime) {
        set(recordingId, callId, returnValue, thrown);
        this.nanoTime = nanoTime;
    }

    @Override
    public String toString() {
        return "ExitRecordQueueEvent{" +
                "recordingId=" + recordingId +
                ", callId=" + callId +
                ", returnValue=" + returnValue +
                ", thrown=" + thrown +
                ", nanoTime=" + nanoTime +
                '}';
    }
}
