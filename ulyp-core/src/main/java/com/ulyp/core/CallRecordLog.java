package com.ulyp.core;

import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class CallRecordLog {

    public static final AtomicInteger idGenerator = new AtomicInteger(-1);

    private final RecordingMetadata recordingMetadata;
    private final TypeResolver typeResolver;
    private final CallEnterRecordList enterRecords = new CallEnterRecordList();
    private final CallExitRecordList exitRecords = new CallExitRecordList();
    private final RecordedMethodCallList recordedCalls = new RecordedMethodCallList();
    private final StackTraceElement[] stackTrace;

    private boolean inProcessOfRecording = true;

    private long lastExitCallId = -1;
    private long rootCallId;
    private long nextCallId;

    public CallRecordLog(TypeResolver typeResolver, long callIdInitialValue) {
        this.recordingMetadata = RecordingMetadata.builder()
                .id(idGenerator.incrementAndGet())
                .createEpochMillis(System.currentTimeMillis())
                .threadId(Thread.currentThread().getId())
                .threadName(Thread.currentThread().getName())
                .build();

        this.typeResolver = typeResolver;
        this.nextCallId = callIdInitialValue;
        this.rootCallId = callIdInitialValue;

        StackTraceElement[] wholeStackTrace = new Exception().getStackTrace();
        // If code changed, there should be a readjustement
        this.stackTrace = Arrays.copyOfRange(wholeStackTrace, 4, wholeStackTrace.length);
    }

    private CallRecordLog(
            RecordingMetadata recordingMetadata,
            TypeResolver typeResolver,
            StackTraceElement[] stackTrace,
            boolean inProcessOfRecording,
            long nextCallId,
            long rootCallId)
    {
        this.recordingMetadata = recordingMetadata;
        this.typeResolver = typeResolver;
        this.stackTrace = stackTrace;
        this.inProcessOfRecording = inProcessOfRecording;
        this.nextCallId = nextCallId;
        this.rootCallId = rootCallId;
    }

    public CallRecordLog cloneWithoutData() {
        return new CallRecordLog(recordingMetadata, this.typeResolver, this.stackTrace, this.inProcessOfRecording, this.nextCallId, rootCallId);
    }

    public long estimateBytesSize() {
        return recordedCalls.getRawBytes().byteLength();
    }

    public long onMethodEnter(Method method, @Nullable Object callee, Object[] args) {
        if (!inProcessOfRecording) {
            return -1;
        }
        inProcessOfRecording = false;
        try {

            long callId = nextCallId++;
            recordedCalls.addEnterMethodCall(
                    recordingMetadata.getId(),
                    callId,
                    method,
                    typeResolver,
                    callee,
                    args
            );
            return callId;
        } finally {
            inProcessOfRecording = true;
        }
    }

    public void onMethodExit(Method method, ObjectRecorder resultRecorder, Object returnValue, Throwable thrown, long callId) {
        if (!inProcessOfRecording) {
            return;
        }

        inProcessOfRecording = false;
        try {
            if (callId >= 0) {
                if (thrown == null) {
                    recordedCalls.addExitMethodCall(
                            recordingMetadata.getId(),
                            callId,
                            method,
                            typeResolver,
                            false,
                            returnValue
                    );
                } else {
                    recordedCalls.addExitMethodCall(
                            recordingMetadata.getId(),
                            callId,
                            method,
                            typeResolver,
                            true,
                            thrown
                    );
                }
                lastExitCallId = callId;
            }
        } finally {
            inProcessOfRecording = true;
        }
    }

    public boolean isComplete() {
        return lastExitCallId == rootCallId;
    }

    public long size() {
        return enterRecords.size();
    }

    public RecordingMetadata getRecordingMetadata() {
        return recordingMetadata;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public RecordedMethodCallList getRecordedCalls() {
        return recordedCalls;
    }

    @Override
    public String toString() {
        return "CallRecordLog{id=" + recordingMetadata + '}';
    }
}
