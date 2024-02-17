package com.ulyp.agent.queue.events;

import com.ulyp.core.pool.PooledObject;

import lombok.Getter;

@Getter
public class ExitRecordQueueEvent extends PooledObject {

    protected int recordingId;
    protected int callId;
    protected Object returnValue;
    protected boolean thrown;

    public void set(int recordingId, int callId, Object returnValue, boolean thrown) {
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
