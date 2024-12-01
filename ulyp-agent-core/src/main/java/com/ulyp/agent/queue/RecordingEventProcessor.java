package com.ulyp.agent.queue;

import com.ulyp.agent.AgentDataWriter;
import com.ulyp.agent.RecordingThreadLocalContext;
import com.ulyp.agent.queue.events.*;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.TypeResolver;

import com.ulyp.core.mem.DirectBufMemPageAllocator;
import com.ulyp.core.mem.MemPageAllocator;
import com.ulyp.core.mem.SerializedRecordedMethodCallList;
import com.ulyp.core.util.SystemPropertyUtil;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Processes all events for a certain recording session. All events are serialized in a flat byte buffer and at some point
 * dropped to {@link AgentDataWriter}
 */
@Slf4j
@NotThreadSafe
public class RecordingEventProcessor {

    private static final int FLUSH_BUFFER_SIZE = SystemPropertyUtil.getInt("ulyp.recording-queue.serialization-buffer-size", 256 * 1024);

    private final TypeResolver typeResolver;
    private final AgentDataWriter agentDataWriter;
    private int recordingId;
    private RecordingMetadata recordingMetadata;
    private MemPageAllocator pageAllocator;
    private SerializedRecordedMethodCallList output;
    private Object[] oneArgArrayCache = new Object[1];
    private Object[] twoArgsArrayCache = new Object[2];
    private Object[] threeArgsArrayCache = new Object[3];

    public RecordingEventProcessor(TypeResolver typeResolver, AgentDataWriter agentDataWriter) {
        this.typeResolver = typeResolver;
        this.agentDataWriter = agentDataWriter;
        this.pageAllocator = new DirectBufMemPageAllocator();
    }

    void onRecordingStarted(RecordingStartedEvent update) {
        recordingMetadata = update.getRecordingMetadata();
    }

    void onEnterCallRecord(int recordingId, EnterMethodRecordingEvent enterRecord) {
        ensureOutputInitialized(recordingId);

        long nanoTime = (enterRecord instanceof TimestampedEnterMethodRecordingEvent) ? ((TimestampedEnterMethodRecordingEvent) enterRecord).getNanoTime() : -1;
        output.addEnterMethodCall(
                enterRecord.getMethodId(),
                typeResolver,
                enterRecord.getCallee(),
                enterRecord.getArgs(),
                nanoTime
        );
    }

    void onEnterCallRecord(int recordingId, EnterMethodOneArgRecordingEvent enterRecord) {
        ensureOutputInitialized(recordingId);

        long nanoTime = (enterRecord instanceof TimestampedEnterMethodOneArgRecordingEvent) ? ((TimestampedEnterMethodOneArgRecordingEvent) enterRecord).getNanoTime() : -1;
        oneArgArrayCache[0] = enterRecord.getArg();
        output.addEnterMethodCall(
                enterRecord.getMethodId(),
                typeResolver,
                enterRecord.getCallee(),
                oneArgArrayCache,
                nanoTime
        );
        oneArgArrayCache[0] = null;
    }

    void onEnterCallRecord(int recordingId, EnterMethodTwoArgsRecordingEvent enterRecord) {
        ensureOutputInitialized(recordingId);

        long nanoTime = (enterRecord instanceof TimestampedEnterMethodTwoArgsRecordingEvent) ? ((TimestampedEnterMethodTwoArgsRecordingEvent) enterRecord).getNanoTime() : -1;
        twoArgsArrayCache[0] = enterRecord.getArg1();
        twoArgsArrayCache[1] = enterRecord.getArg2();
        output.addEnterMethodCall(
                enterRecord.getMethodId(),
                typeResolver,
                enterRecord.getCallee(),
                twoArgsArrayCache,
                nanoTime
        );
        twoArgsArrayCache[0] = null;
        twoArgsArrayCache[1] = null;
    }

    public void onEnterCallRecord(int recordingId, EnterMethodThreeArgsRecordingEvent enterRecord) {
        ensureOutputInitialized(recordingId);

        long nanoTime = (enterRecord instanceof TimestampedEnterMethodThreeArgsRecordingEvent) ? ((TimestampedEnterMethodThreeArgsRecordingEvent) enterRecord).getNanoTime() : -1;
        threeArgsArrayCache[0] = enterRecord.getArg1();
        threeArgsArrayCache[1] = enterRecord.getArg2();
        threeArgsArrayCache[2] = enterRecord.getArg3();
        output.addEnterMethodCall(
                enterRecord.getMethodId(),
                typeResolver,
                enterRecord.getCallee(),
                threeArgsArrayCache,
                nanoTime
        );
        threeArgsArrayCache[0] = null;
        threeArgsArrayCache[1] = null;
        threeArgsArrayCache[2] = null;
    }

    void onEnterCallRecord(int recordingId, EnterMethodNoArgsRecordingEvent enterRecord) {
        ensureOutputInitialized(recordingId);

        long nanoTime = (enterRecord instanceof TimestampedEnterMethodNoArgsRecordingEvent) ? ((TimestampedEnterMethodNoArgsRecordingEvent) enterRecord).getNanoTime() : -1;
        output.addEnterMethodCall(
                enterRecord.getMethodId(),
                typeResolver,
                enterRecord.getCallee(),
                null,
                nanoTime
        );
    }

    public void onRecordingFinished(RecordingFinishedEvent event) {
        recordingMetadata = recordingMetadata.withCompleteTime(event.getFinishTimeMillis());
        agentDataWriter.write(typeResolver, recordingMetadata, output);
        this.output = null;
    }

    void onExitCallRecord(int recordingId, ExitMethodRecordingEvent exitRecord) {
        SerializedRecordedMethodCallList recordedCalls = this.output;
        if (recordedCalls == null) {
            log.debug("Call record buffer not found for recording id " + recordingId);
            return;
        }
        int callId = exitRecord.getCallId();
        long nanoTime = (exitRecord instanceof TimestampedExitMethodRecordingEvent) ? ((TimestampedExitMethodRecordingEvent) exitRecord).getNanoTime() : -1;
        if (exitRecord.isThrown()) {
            recordedCalls.addExitMethodThrow(callId, typeResolver, exitRecord.getReturnValue(), nanoTime);
        } else {
            recordedCalls.addExitMethodCall(callId, typeResolver, exitRecord.getReturnValue(), nanoTime);
        }

        writeOutputIfNeeded(recordedCalls, callId);
    }

    private void ensureOutputInitialized(int recordingId) {
        if (output == null) {
            this.recordingId = recordingId;
            output = new SerializedRecordedMethodCallList(recordingId, pageAllocator);
        }
    }

    private void writeOutputIfNeeded(SerializedRecordedMethodCallList recordedCalls, int callId) {
        if (recordedCalls.bytesWritten() > FLUSH_BUFFER_SIZE) {

            if (callId != RecordingThreadLocalContext.ROOT_CALL_RECORDING_ID) {
                this.output = new SerializedRecordedMethodCallList(this.recordingId, pageAllocator);
            }

            agentDataWriter.write(typeResolver, recordingMetadata, recordedCalls);

            if (callId == RecordingThreadLocalContext.ROOT_CALL_RECORDING_ID) {
                this.output = null;
            }
        }
    }
}
