package com.ulyp.agent.queue.events;

import lombok.Getter;

@Getter
public class ExitMethodRecordingEvent implements RecordingEvent {

    protected final int callId;
    protected final Object returnValue;
    protected final boolean thrown;

    public ExitMethodRecordingEvent(int callId, Object returnValue, boolean thrown) {
        this.callId = callId;
        this.returnValue = returnValue;
        this.thrown = thrown;
    }

    @Override
    public String toString() {
        return "ExitRecordQueueEvent{" +
                "callId=" + callId +
                ", returnValue=" + returnValue +
                ", thrown=" + thrown +
                '}';
    }
}
