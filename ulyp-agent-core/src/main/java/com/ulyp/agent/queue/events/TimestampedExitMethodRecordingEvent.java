package com.ulyp.agent.queue.events;

import lombok.Getter;

@Getter
public class TimestampedExitMethodRecordingEvent extends ExitMethodRecordingEvent {

    private final long nanoTime;

    public TimestampedExitMethodRecordingEvent(int callId, Object returnValue, boolean thrown, long nanoTime) {
        super(callId, returnValue, thrown);
        this.nanoTime = nanoTime;
    }

    @Override
    public String toString() {
        return "ExitRecordQueueEvent{" +
                "callId=" + callId +
                ", returnValue=" + returnValue +
                ", thrown=" + thrown +
                ", nanoTime=" + nanoTime +
                '}';
    }
}
