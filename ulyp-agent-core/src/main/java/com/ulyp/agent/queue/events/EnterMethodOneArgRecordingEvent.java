package com.ulyp.agent.queue.events;

import lombok.Getter;

/**
 * A recording event which is created and stored into {@link com.ulyp.agent.RecordingEventBuffer} when a method is called
 * while recording is active.
 */
@Getter
public class EnterMethodOneArgRecordingEvent extends AbstractEnterMethodRecordingEvent {

    protected Object arg;

    public EnterMethodOneArgRecordingEvent(int methodId, Object callee, Object arg) {
        super(methodId, callee);
        this.arg = arg;
    }
}
