package com.ulyp.agent.queue.events;

import lombok.Getter;

/**
 * A recording event which is created and stored into {@link com.ulyp.agent.RecordingEventBuffer} when a method is called
 * while recording is active.
 * Additionally, carries nano time. This class is only used if recording of timestamps is enabled.
 */
@Getter
public class TimestampedEnterMethodOneArgRecordingEvent extends EnterMethodOneArgRecordingEvent {

    private final long nanoTime;

    public TimestampedEnterMethodOneArgRecordingEvent(int methodId, Object callee, Object arg, long nanoTime) {
        super(methodId, callee, arg);
        this.nanoTime = nanoTime;
    }
}
