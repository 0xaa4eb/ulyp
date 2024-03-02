package com.ulyp.core;

import com.ulyp.core.mem.MemPageAllocator;
import com.ulyp.core.mem.SerializedRecordedMethodCallList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * A collection of enter and exit recorded method calls for a certain recording session.
 */
@NotThreadSafe
@Slf4j
public class CallRecordBuffer {

    @Getter
    private final SerializedRecordedMethodCallList recordedCalls;
    private final int recordingId;
    private final MemPageAllocator pageAllocator;
    private int lastExitCallId = -1;

    public CallRecordBuffer(int recordingId, MemPageAllocator pageAllocator) {
        this.pageAllocator = pageAllocator;
        this.recordingId = recordingId;
        this.recordedCalls = new SerializedRecordedMethodCallList(recordingId, pageAllocator);
    }

    public CallRecordBuffer cloneWithoutData() {
        return new CallRecordBuffer(this.recordingId, pageAllocator);
    }

    public long estimateBytesSize() {
        return recordedCalls.bytesWritten();
    }

    public void recordMethodEnter(int callId, TypeResolver typeResolver, int methodId, @Nullable Object callee, Object[] args, long nanoTime) {
        try {
            recordedCalls.addEnterMethodCall(callId, methodId, typeResolver, callee, args, nanoTime);
        } catch (Throwable err) {
            // catch Throwable intentionally. While recording is done anything can happen, but the app which uses ulyp should not be disrupted
            log.error("Error while recording", err);
        }
    }

    public void recordMethodExit(TypeResolver typeResolver, Object returnValue, Throwable thrown, int callId, long nanoTime) {
        if (callId >= 0) {
            if (thrown == null) {
                recordedCalls.addExitMethodCall(callId, typeResolver, returnValue, nanoTime);
            } else {
                recordedCalls.addExitMethodThrow(callId, typeResolver, thrown, nanoTime);
            }
            lastExitCallId = callId;
        }
    }

    public boolean isComplete() {
        return lastExitCallId == 1;
    }

    @Override
    public String toString() {
        return "CallRecordBuffer{}";
    }
}
