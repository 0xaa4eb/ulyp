package com.ulyp.agent.queue.events;

import lombok.Getter;

import java.util.Arrays;

import com.ulyp.core.pool.PooledObject;

@Getter
public class EnterRecordQueueEvent extends PooledObject {

    protected int recordingId;
    protected int callId;
    protected int methodId;
    protected int calleeTypeId;
    protected int calleeIdentityHashCode;
    protected Object[] args;

    public void set(int recordingId, int callId, int methodId, int calleeTypeId, int calleeIdentityHashCode, Object[] args) {
        this.recordingId = recordingId;
        this.callId = callId;
        this.methodId = methodId;
        this.calleeTypeId = calleeTypeId;
        this.calleeIdentityHashCode = calleeIdentityHashCode;
        this.args = args;
    }

    @Override
    public String toString() {
        return "EnterRecordQueueEvent{" +
                "recordingId=" + recordingId +
                ", callId=" + callId +
                ", methodId=" + methodId +
                ", calleeTypeId=" + calleeTypeId +
                ", calleeIdentityHashCode=" + calleeIdentityHashCode +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
