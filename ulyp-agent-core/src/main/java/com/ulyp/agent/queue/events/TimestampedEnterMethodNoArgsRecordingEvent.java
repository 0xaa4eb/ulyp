package com.ulyp.agent.queue.events;

import lombok.Getter;

import java.util.Arrays;

/**
 * A recording event which is created and stored into {@link com.ulyp.agent.RecordingEventBuffer} when a method is called
 * while recording is active.
 * Additionally, carries nano time. This class is only used if recording of timestamps is enabled.
 */
@Getter
public class TimestampedEnterMethodNoArgsRecordingEvent extends EnterMethodNoArgsRecordingEvent {

    private final long nanoTime;

    public TimestampedEnterMethodNoArgsRecordingEvent(int methodId, Object callee, long nanoTime) {
        super(methodId, callee);
        this.nanoTime = nanoTime;
    }

    @Override
    public String toString() {
        return "EnterRecordQueueEvent{" +
                "methodId=" + methodId +
                ", callee=" + callee +
                ", nanoTime=" + nanoTime +
                '}';
    }
}
