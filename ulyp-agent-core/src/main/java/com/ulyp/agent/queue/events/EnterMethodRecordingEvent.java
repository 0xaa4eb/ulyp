package com.ulyp.agent.queue.events;

import lombok.Getter;

import java.util.Arrays;

/**
 * A recording event which is created and stored into {@link com.ulyp.agent.RecordingEventBuffer} when a method is called
 * while recording is active.
 */
@Getter
public class EnterMethodRecordingEvent extends AbstractEnterMethodRecordingEvent {

    protected final Object[] args;

    public EnterMethodRecordingEvent(int methodId, Object callee, Object[] args) {
        super(methodId, callee);
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
