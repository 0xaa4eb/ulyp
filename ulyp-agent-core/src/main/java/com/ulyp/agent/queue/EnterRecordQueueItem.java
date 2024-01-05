package com.ulyp.agent.queue;

import com.ulyp.core.RecordingMetadata;

import lombok.Getter;

@Getter
class EnterRecordQueueItem {

    private final RecordingMetadata recordingMetadata;
    private final int callId;
    private final int methodId;
    private final Object callee;
    private final Object[] args;
    private final long nanoTime;

    EnterRecordQueueItem(RecordingMetadata recordingMetadata, int callId, int methodId, Object callee, Object[] args, long nanoTime) {
        this.recordingMetadata = recordingMetadata;
        this.callId = callId;
        this.methodId = methodId;
        this.callee = callee;
        this.args = args;
        this.nanoTime = nanoTime;
    }
}
