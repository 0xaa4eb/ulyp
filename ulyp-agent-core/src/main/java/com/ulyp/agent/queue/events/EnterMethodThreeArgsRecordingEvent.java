package com.ulyp.agent.queue.events;

import lombok.Getter;

/**
 * A recording event which is created and stored into {@link com.ulyp.agent.RecordingEventBuffer} when a method is called
 * while recording is active.
 */
@Getter
public class EnterMethodThreeArgsRecordingEvent extends AbstractEnterMethodRecordingEvent {

    private Object arg1;
    private Object arg2;
    private Object arg3;

    public EnterMethodThreeArgsRecordingEvent(int methodId, Object callee, Object arg1, Object arg2, Object arg3) {
        super(methodId, callee);
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
    }
}
