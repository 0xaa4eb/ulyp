package com.ulyp.agent.queue.events;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class TimestampedEnterMethodRecordingEvent extends EnterMethodRecordingEvent {

    private final long nanoTime;

    public TimestampedEnterMethodRecordingEvent(int callId, int methodId, int calleeTypeId, int calleeIdentityHashCode, Object[] args, long nanoTime) {
        super(callId, methodId, calleeTypeId, calleeIdentityHashCode, args);
        this.nanoTime = nanoTime;
    }

    @Override
    public String toString() {
        return "EnterRecordQueueEvent{" +
                "callId=" + callId +
                ", methodId=" + methodId +
                ", calleeTypeId=" + calleeTypeId +
                ", calleeIdentityHashCode=" + calleeIdentityHashCode +
                ", args=" + Arrays.toString(args) +
                ", nanoTime=" + nanoTime +
                '}';
    }
}
