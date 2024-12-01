package com.ulyp.agent.queue.events;

import lombok.Getter;

/**
 * A recording event which is created and stored into {@link com.ulyp.agent.RecordingEventBuffer} when a method is called
 * while recording is active.
 * Additionally, carries nano time. This class is only used if recording of timestamps is enabled.
 */
@Getter
public class TimestampedEnterMethodTwoArgsRecordingEvent extends EnterMethodTwoArgsRecordingEvent {

    private final long nanoTime;

    public TimestampedEnterMethodTwoArgsRecordingEvent(int methodId, Object callee, Object arg1, Object arg2, long nanoTime) {
        super(methodId, callee, arg1, arg2);
        this.nanoTime = nanoTime;
    }
}
