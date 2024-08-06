package com.ulyp.agent.queue.events;

import lombok.Getter;

import java.util.Arrays;

/**
 * A recording event which is created and stored into {@link com.ulyp.agent.RecordingEventBuffer} when a method is called
 * while recording is active.
 */
@Getter
public class EnterMethodRecordingEvent implements RecordingEvent {

    protected final int methodId;
    protected final Object callee;
    protected final Object[] args;

    public EnterMethodRecordingEvent(int methodId, Object callee, Object[] args) {
        this.methodId = methodId;
        this.callee = callee;
        this.args = args;
    }

    @Override
    public String toString() {
        return "EnterRecordQueueEvent{" +
                "methodId=" + methodId +
                ", callee=" + callee +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
