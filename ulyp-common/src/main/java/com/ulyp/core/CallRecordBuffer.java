package com.ulyp.core;

import com.ulyp.core.mem.RecordedMethodCallList;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.NotThreadSafe;

import lombok.extern.slf4j.Slf4j;

/**
 * A collection of enter and exit recorded method calls for a certain recording session.
 */
@NotThreadSafe
@Slf4j
public class CallRecordBuffer {

    private final RecordedMethodCallList recordedCalls = new RecordedMethodCallList();
    private final long rootCallId;

    private long lastExitCallId = -1;
    private long nextCallId;

    public CallRecordBuffer(long callIdInitialValue) {
        this.nextCallId = callIdInitialValue;
        this.rootCallId = callIdInitialValue;
    }

    private CallRecordBuffer(long nextCallId, long rootCallId) {
        this.nextCallId = nextCallId;
        this.rootCallId = rootCallId;
    }

    public CallRecordBuffer cloneWithoutData() {
        return new CallRecordBuffer(this.nextCallId, rootCallId);
    }

    public long estimateBytesSize() {
        return recordedCalls.getRawBytes().byteLength();
    }

    public long recordMethodEnter(TypeResolver typeResolver, int recordingId, Method method, @Nullable Object callee, Object[] args) {
        try {

            long callId = nextCallId++;
            recordedCalls.addEnterMethodCall(
                    recordingId,
                    callId,
                    method,
                    typeResolver,
                    callee,
                    args
            );
            return callId;
        } catch (Throwable err) {
            // catch Throwable intentionally. While recording is done anything can happen, but the app which uses ulyp should not be disrupted
            log.error("Error while recording", err);
            return -1;
        }
    }

    public void recordMethodExit(TypeResolver typeResolver, int recordingId, Method method, Object returnValue, Throwable thrown, long callId) {
        if (callId >= 0) {
            if (thrown == null) {
                recordedCalls.addExitMethodCall(
                    recordingId,
                    callId,
                    method,
                    typeResolver,
                    false,
                    returnValue
                );
            } else {
                recordedCalls.addExitMethodCall(
                    recordingId,
                    callId,
                    method,
                    typeResolver,
                    true,
                    thrown
                );
            }
            lastExitCallId = callId;
        }
    }

    public boolean isComplete() {
        return lastExitCallId == rootCallId;
    }

    public long getTotalRecordedEnterCalls() {
        return nextCallId - rootCallId;
    }

    public int getRecordedCallsSize() {
        return recordedCalls.size();
    }

    public RecordedMethodCallList getRecordedCalls() {
        return recordedCalls;
    }

    @Override
    public String toString() {
        return "CallRecordBuffer{}";
    }
}
