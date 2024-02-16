package com.ulyp.agent.queue.events;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class EnterRecordQueueEvent {

    protected final int recordingId;
    protected final int callId;
    protected final int methodId;
    protected final int calleeTypeId;
    protected final int calleeIdentityHashCode;
    protected final Object[] args;

    public EnterRecordQueueEvent(int recordingId, int callId, int methodId, int calleeTypeId, int calleeIdentityHashCode, Object[] args) {
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
