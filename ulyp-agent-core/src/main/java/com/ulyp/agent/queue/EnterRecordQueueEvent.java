package com.ulyp.agent.queue;

import lombok.Getter;

@Getter
class EnterRecordQueueEvent {

    private final int recordingId;
    private final int callId;
    private final int methodId;
    private final Object callee;
    private final Object[] args;
    private final long nanoTime;

    EnterRecordQueueEvent(int recordingId, int callId, int methodId, Object callee, Object[] args, long nanoTime) {
        this.recordingId = recordingId;
        this.callId = callId;
        this.methodId = methodId;
        this.callee = callee;
        this.args = args;
        this.nanoTime = nanoTime;
    }
}
