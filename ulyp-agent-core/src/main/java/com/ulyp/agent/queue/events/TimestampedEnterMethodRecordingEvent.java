package com.ulyp.agent.queue.events;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class TimestampedEnterMethodRecordingEvent extends EnterMethodRecordingEvent {

    private final long nanoTime;

    public TimestampedEnterMethodRecordingEvent(int callId, int methodId, Object callee, Object[] args, long nanoTime) {
        super(callId, methodId, callee, args);
        this.nanoTime = nanoTime;
    }

    @Override
    public String toString() {
        return "EnterRecordQueueEvent{" +
                "callId=" + callId +
                ", methodId=" + methodId +
                ", callee=" + callee +
                ", args=" + Arrays.toString(args) +
                ", nanoTime=" + nanoTime +
                '}';
    }
}
