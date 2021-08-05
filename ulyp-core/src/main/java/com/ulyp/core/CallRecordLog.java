package com.ulyp.core;

import com.ulyp.core.printers.ObjectBinaryPrinter;
import com.ulyp.core.printers.ObjectBinaryPrinterType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class CallRecordLog {

    public static final AtomicLong idGenerator = new AtomicLong(-1L);

    private final long recordingId;
    private final long chunkId;
    private final TypeResolver typeResolver;
    private final CallEnterRecordList enterRecords = new CallEnterRecordList();
    private final CallExitRecordList exitRecords = new CallExitRecordList();
    private final long epochMillisCreatedTime = System.currentTimeMillis();
    private final String threadName;
    private final long threadId;
    private final StackTraceElement[] stackTrace;

    private boolean inProcessOfRecording = true;
    private long callsRecorded = 0;
    private long lastExitCallId = -1;
    private long rootCallId;
    private long callIdCounter;

    public CallRecordLog(TypeResolver typeResolver, long callIdInitialValue) {
        this.chunkId = 0;
        this.recordingId = idGenerator.incrementAndGet();
        this.typeResolver = typeResolver;
        this.callIdCounter = callIdInitialValue;
        this.rootCallId = callIdInitialValue;

        StackTraceElement[] wholeStackTrace = new Exception().getStackTrace();

        // If code changed, there should be a readjustement
        this.stackTrace = Arrays.copyOfRange(wholeStackTrace, 4, wholeStackTrace.length);
        this.threadName = Thread.currentThread().getName();
        this.threadId = Thread.currentThread().getId();
    }

    private CallRecordLog(
            long chunkId,
            long recordingId,
            TypeResolver typeResolver,
            String threadName,
            long threadId,
            StackTraceElement[] stackTrace,
            boolean inProcessOfRecording,
            long callIdCounter,
            long rootCallId,
            long callsRecorded)
    {
        this.chunkId = chunkId;
        this.recordingId = recordingId;
        this.typeResolver = typeResolver;
        this.threadName = threadName;
        this.threadId = threadId;
        this.stackTrace = stackTrace;
        this.inProcessOfRecording = inProcessOfRecording;
        this.callIdCounter = callIdCounter;
        this.rootCallId = rootCallId;
        this.callsRecorded = callsRecorded;
    }

    public CallRecordLog cloneWithoutData() {
        return new CallRecordLog(this.chunkId + 1, this.recordingId, this.typeResolver, this.threadName, this.threadId, this.stackTrace, this.inProcessOfRecording, this.callIdCounter, rootCallId, callsRecorded);
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
            callsRecorded++;
            enterRecords.add(callId, method.getId(), typeResolver, method.getParamPrinters(), callee, args);
            return callId;
        } finally {
            inProcessOfRecording = true;
        }
    }

    public void onMethodExit(long methodId, ObjectBinaryPrinter resultPrinter, Object returnValue, Throwable thrown, long callId) {
        if (!inProcessOfRecording) {
            return;
        }

        inProcessOfRecording = false;
        try {
            if (callId >= 0) {
                if (thrown == null) {
                    exitRecords.add(callId, methodId, typeResolver, false, resultPrinter, returnValue);
                } else {
                    exitRecords.add(callId, methodId, typeResolver, true, ObjectBinaryPrinterType.THROWABLE_PRINTER.getInstance(), thrown);
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

    public long getChunkId() {
        return chunkId;
    }

    public String getThreadName() {
        return threadName;
    }

    public long getThreadId() {
        return threadId;
    }

    public long getLastCallId() {
        return callIdCounter;
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

    public long getRecordingId() {
        return recordingId;
    }

    public long getEpochMillisCreatedTime() {
        return epochMillisCreatedTime;
    }

    public long getCallsRecorded() {
        return callsRecorded;
    }

    @Override
    public String toString() {
        return "CallRecordLog{" +
                "id=" + recordingId + '}';
    }
}
