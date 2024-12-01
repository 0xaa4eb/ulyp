package com.ulyp.agent.queue.events;

import lombok.Getter;

/**
 * A recording event which is created and stored into {@link com.ulyp.agent.RecordingEventBuffer} when a method is called
 * while recording is active.
 */
@Getter
public class EnterMethodTwoArgsRecordingEvent extends AbstractEnterMethodRecordingEvent {

    private Object arg1;
    private Object arg2;

    public EnterMethodTwoArgsRecordingEvent(int methodId, Object callee, Object arg1, Object arg2) {
        super(methodId, callee);
        this.arg1 = arg1;
        this.arg2 = arg2;
    }
}
