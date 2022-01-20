package com.ulyp.core;

import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.RecorderType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class CallRecordLog {

    public static final AtomicInteger idGenerator = new AtomicInteger(-1);

    private final RecordingMetadata recordingMetadata;
    private final TypeResolver typeResolver;
    private final CallEnterRecordList enterRecords = new CallEnterRecordList();
    private final CallExitRecordList exitRecords = new CallExitRecordList();
    private final StackTraceElement[] stackTrace;

    private boolean inProcessOfRecording = true;

    private long lastExitCallId = -1;
    private long rootCallId;
    private long callIdCounter;

    public CallRecordLog(TypeResolver typeResolver, long callIdInitialValue) {
        this.recordingMetadata = RecordingMetadata.builder()
                .id(idGenerator.incrementAndGet())
                .createEpochMillis(System.currentTimeMillis())
                .threadId(Thread.currentThread().getId())
                .threadName(Thread.currentThread().getName())
                .build();

        this.typeResolver = typeResolver;
        this.callIdCounter = callIdInitialValue;
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
            long callIdCounter,
            long rootCallId)
    {
        this.recordingMetadata = recordingMetadata;
        this.typeResolver = typeResolver;
        this.stackTrace = stackTrace;
        this.inProcessOfRecording = inProcessOfRecording;
        this.callIdCounter = callIdCounter;
        this.rootCallId = rootCallId;
    }

    public CallRecordLog cloneWithoutData() {
        return new CallRecordLog(recordingMetadata, this.typeResolver, this.stackTrace, this.inProcessOfRecording, this.callIdCounter, rootCallId);
    }

    public long estimateBytesSize() {
        return enterRecords.buffer.capacity() + exitRecords.buffer.capacity();
    }

    public long onMethodEnter(Method method, @Nullable Object callee, Object[] args) {
        if (!inProcessOfRecording) {
            return -1;
        }
        inProcessOfRecording = false;
        try {

            long callId = callIdCounter++;
            enterRecords.add(callId, method.getId(), typeResolver, method.getParameterRecorders(), callee, args);
            return callId;
        } finally {
            inProcessOfRecording = true;
        }
    }

    public void onMethodExit(long methodId, ObjectRecorder resultRecorder, Object returnValue, Throwable thrown, long callId) {
        if (!inProcessOfRecording) {
            return;
        }

        inProcessOfRecording = false;
        try {
            if (callId >= 0) {
                if (thrown == null) {
                    exitRecords.add(callId, methodId, typeResolver, false, resultRecorder, returnValue);
                } else {
                    exitRecords.add(callId, methodId, typeResolver, true, RecorderType.THROWABLE_RECORDER.getInstance(), thrown);
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

    public CallEnterRecordList getEnterRecords() {
        return enterRecords;
    }

    public CallExitRecordList getExitRecords() {
        return exitRecords;
    }

    @Override
    public String toString() {
        return "CallRecordLog{id=" + recordingMetadata + '}';
    }
}
