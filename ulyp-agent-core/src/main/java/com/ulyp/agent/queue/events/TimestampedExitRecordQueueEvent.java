package com.ulyp.agent.queue.events;

import lombok.Getter;

@Getter
public class TimestampedExitRecordQueueEvent extends ExitRecordQueueEvent {

    private final long nanoTime;

    public TimestampedExitRecordQueueEvent(int recordingId, int callId, Object returnValue, boolean thrown, long nanoTime) {
        super(recordingId, callId, returnValue, thrown);
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
