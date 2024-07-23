package com.ulyp.agent.queue.events;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class EnterMethodRecordingEvent implements RecordingEvent {

    protected final int callId;
    protected final int methodId;
    protected final Object callee;
    protected final Object[] args;

    public EnterMethodRecordingEvent(int callId, int methodId, Object callee, Object[] args) {
        this.callId = callId;
        this.methodId = methodId;
        this.callee = callee;
        this.args = args;
    }

    @Override
    public String toString() {
        return "EnterRecordQueueEvent{" +
                "callId=" + callId +
                ", methodId=" + methodId +
                ", callee=" + callee +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
