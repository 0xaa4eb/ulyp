package com.ulyp.core;

import com.ulyp.core.printers.ObjectBinaryPrinter;
import com.ulyp.core.printers.ObjectBinaryPrinterType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class CallRecordLog {

    public static final AtomicLong idGenerator = new AtomicLong(3000000000L);

    private final long recordingSessionId;
    private final long chunkId;
    private final TypeResolver typeResolver;
    private final CallEnterRecordList enterRecords = new CallEnterRecordList();
    private final CallExitRecordList exitRecords = new CallExitRecordList();
    private final long epochMillisCreatedTime = System.currentTimeMillis();
    private final String threadName;
    private final long threadId;
    private final StackTraceElement[] stackTrace;
    private final int maxDepth;
    private final int maxCallsToRecordPerMethod;

    private boolean inProcessOfTracing = true;
    private long lastExitCallId = -1;
    private long callIdCounter = 0;

    public CallRecordLog(TypeResolver typeResolver, int maxDepth, int maxCallsToRecordPerMethod) {
        this.chunkId = 0;
        this.recordingSessionId = idGenerator.incrementAndGet();
        this.maxDepth = maxDepth;
        this.maxCallsToRecordPerMethod = maxCallsToRecordPerMethod;
        this.typeResolver = typeResolver;

        StackTraceElement[] wholeStackTrace = new Exception().getStackTrace();

        // If code changed, there should be a readjustement
        this.stackTrace = Arrays.copyOfRange(wholeStackTrace, 4, wholeStackTrace.length);
        this.threadName = Thread.currentThread().getName();
        this.threadId = Thread.currentThread().getId();
    }

    private CallRecordLog(
            long chunkId,
            long recordingSessionId,
            TypeResolver typeResolver,
            String threadName,
            long threadId,
            StackTraceElement[] stackTrace,
            int maxDepth,
            int maxCallsToRecordPerMethod,
            boolean inProcessOfTracing,
            long callIdCounter)
    {
        this.chunkId = chunkId;
        this.recordingSessionId = recordingSessionId;
        this.typeResolver = typeResolver;
        this.threadName = threadName;
        this.threadId = threadId;
        this.stackTrace = stackTrace;
        this.maxDepth = maxDepth;
        this.maxCallsToRecordPerMethod = maxCallsToRecordPerMethod;
        this.inProcessOfTracing = inProcessOfTracing;
        this.callIdCounter = callIdCounter;
    }

    public CallRecordLog cloneWithoutData() {
        return new CallRecordLog(this.chunkId + 1, this.recordingSessionId, this.typeResolver, this.threadName, this.threadId, this.stackTrace, this.maxDepth, this.maxCallsToRecordPerMethod, this.inProcessOfTracing, this.callIdCounter);
    }

    public long estimateBytesSize() {
        return enterRecords.buffer.capacity() + exitRecords.buffer.capacity();
    }

    public long onMethodEnter(int methodId, ObjectBinaryPrinter[] printers, @Nullable Object callee, Object[] args) {
        if (!inProcessOfTracing) {
            return -1;
        }
        inProcessOfTracing = false;
        try {

            long callId = callIdCounter++;
            enterRecords.add(callId, methodId, typeResolver, printers, callee, args);
            return callId;
        } finally {
            inProcessOfTracing = true;
        }
    }

    public void onMethodExit(int methodId, ObjectBinaryPrinter resultPrinter, Object returnValue, Throwable thrown, long callId) {
        if (!inProcessOfTracing) {
            return;
        }

        inProcessOfTracing = false;
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
            inProcessOfTracing = true;
        }
    }

    public boolean isComplete() {
        return lastExitCallId == 0;
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

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

    public CallEnterRecordList getEnterRecords() {
        return enterRecords;
    }

    public CallExitRecordList getExitRecords() {
        return exitRecords;
    }

    public long getRecordingSessionId() {
        return recordingSessionId;
    }

    public long getEpochMillisCreatedTime() {
        return epochMillisCreatedTime;
    }

    @Override
    public String toString() {
        return "CallRecordLog{" +
                "id=" + recordingSessionId + '}';
    }
}
