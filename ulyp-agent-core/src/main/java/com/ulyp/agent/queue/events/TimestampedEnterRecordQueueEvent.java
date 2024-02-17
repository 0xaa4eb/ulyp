package com.ulyp.agent.queue.events;

import lombok.Getter;

import java.util.Arrays;

@Getter
public class TimestampedEnterRecordQueueEvent extends EnterRecordQueueEvent {

    private long nanoTime;

    public void set(int recordingId, int callId, int methodId, int calleeTypeId, int calleeIdentityHashCode, Object[] args, long nanoTime) {
        set(recordingId, callId, methodId, calleeTypeId, calleeIdentityHashCode, args);
        this.nanoTime = nanoTime;
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
                ", nanoTime=" + nanoTime +
                '}';
    }
}
