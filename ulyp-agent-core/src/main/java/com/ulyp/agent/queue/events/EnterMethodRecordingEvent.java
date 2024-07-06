package com.ulyp.agent.queue.events;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class EnterMethodRecordingEvent implements RecordingEvent {

    protected final int callId;
    protected final int methodId;
    protected final int calleeTypeId;
    protected final int calleeIdentityHashCode;
    protected final Object[] args;

    public EnterMethodRecordingEvent(int callId, int methodId, int calleeTypeId, int calleeIdentityHashCode, Object[] args) {
        this.callId = callId;
        this.methodId = methodId;
        this.calleeTypeId = calleeTypeId;
        this.calleeIdentityHashCode = calleeIdentityHashCode;
        this.args = args;
    }

    @Override
    public String toString() {
        return "EnterRecordQueueEvent{" +
                "callId=" + callId +
                ", methodId=" + methodId +
                ", calleeTypeId=" + calleeTypeId +
                ", calleeIdentityHashCode=" + calleeIdentityHashCode +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
