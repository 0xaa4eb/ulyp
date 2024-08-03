package com.ulyp.agent.queue.events;

import lombok.Getter;

import java.util.Arrays;

/**
 * A recording event which is created and stored into {@link com.ulyp.agent.RecordingEventBuffer} when a method is called
 * while recording is active.
 * Additionally, carries nano time. This class is only used if recording of timestamps is enabled.
 */
@Getter
public class TimestampedEnterMethodRecordingEvent extends EnterMethodRecordingEvent {

    private final long nanoTime;

    public TimestampedEnterMethodRecordingEvent(int methodId, Object callee, Object[] args, long nanoTime) {
        super(methodId, callee, args);
        this.nanoTime = nanoTime;
    }

    @Override
    public String toString() {
        return "EnterRecordQueueEvent{" +
                "methodId=" + methodId +
                ", callee=" + callee +
                ", args=" + Arrays.toString(args) +
                ", nanoTime=" + nanoTime +
                '}';
    }
}
