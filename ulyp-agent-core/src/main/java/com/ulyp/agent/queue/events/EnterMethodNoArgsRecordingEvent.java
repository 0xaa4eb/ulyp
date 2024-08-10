package com.ulyp.agent.queue.events;

import lombok.Getter;

import java.util.Arrays;

/**
 * A recording event which is created and stored into {@link com.ulyp.agent.RecordingEventBuffer} when a method is called
 * while recording is active.
 */
@Getter
public class EnterMethodNoArgsRecordingEvent extends AbstractEnterMethodRecordingEvent {

    public EnterMethodNoArgsRecordingEvent(int methodId, Object callee) {
        super(methodId, callee);
    }

    @Override
    public String toString() {
        return "EnterMethodNoArgsRecordingEvent{" +
                "methodId=" + methodId +
                ", callee=" + callee +
                '}';
    }
}
