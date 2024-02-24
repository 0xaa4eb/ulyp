package com.ulyp.agent.queue.events;

import lombok.Getter;

@Getter
public class ExitRecordQueueEvent {

    protected final int recordingId;
    protected final int callId;
    protected final Object returnValue;
    protected final boolean thrown;

    public ExitRecordQueueEvent(int recordingId, int callId, Object returnValue, boolean thrown) {
        this.recordingId = recordingId;
        this.callId = callId;
        this.returnValue = returnValue;
        this.thrown = thrown;
    }

    @Override
    public String toString() {
        return "ExitRecordQueueEvent{" +
                "recordingId=" + recordingId +
                ", callId=" + callId +
                ", returnValue=" + returnValue +
                ", thrown=" + thrown +
                '}';
    }
}
