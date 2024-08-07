package com.ulyp.agent.queue;

import com.ulyp.agent.AgentDataWriter;
import com.ulyp.agent.RecordingState;
import com.ulyp.agent.queue.events.*;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.TypeResolver;

import com.ulyp.core.mem.DirectBufMemPageAllocator;
import com.ulyp.core.mem.MemPageAllocator;
import com.ulyp.core.mem.SerializedRecordedMethodCallList;
import com.ulyp.core.util.SystemPropertyUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RecordingEventProcessor {

    private static final int FLUSH_BUFFER_SIZE = SystemPropertyUtil.getInt("ulyp.recording-queue.serialization-buffer-size", 256 * 1024);

    private final TypeResolver typeResolver;
    private final AgentDataWriter agentDataWriter;
    private int recordingId;
    private RecordingMetadata recordingMetadata;
    private MemPageAllocator pageAllocator;
    private SerializedRecordedMethodCallList recordedCalls;

    public RecordingEventProcessor(TypeResolver typeResolver, AgentDataWriter agentDataWriter) {
        this.typeResolver = typeResolver;
        this.agentDataWriter = agentDataWriter;
        this.pageAllocator = new DirectBufMemPageAllocator();
    }

    void onRecordingStarted(RecordingStartedEvent update) {
        recordingMetadata = update.getRecordingMetadata();
    }

    void onEnterCallRecord(int recordingId, EnterMethodRecordingEvent enterRecord) {
        if (recordedCalls == null) {
            this.recordingId = recordingId;
            recordedCalls = new SerializedRecordedMethodCallList(recordingId, pageAllocator);
        }

        long nanoTime = (enterRecord instanceof TimestampedEnterMethodRecordingEvent) ? ((TimestampedEnterMethodRecordingEvent) enterRecord).getNanoTime() : -1;
        recordedCalls.addEnterMethodCall(
                enterRecord.getMethodId(),
                typeResolver,
                enterRecord.getCallee(),
                enterRecord.getArgs(),
                nanoTime
        );
    }

    public void onRecordingFinished(RecordingFinishedEvent event) {
        recordingMetadata = recordingMetadata.withCompleteTime(event.getFinishTimeMillis());
        agentDataWriter.write(typeResolver, recordingMetadata, recordedCalls);
        this.recordedCalls = null;
    }

    void onExitCallRecord(int recordingId, ExitMethodRecordingEvent exitRecord) {
        SerializedRecordedMethodCallList recordedCalls = this.recordedCalls;
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

        if (recordedCalls.bytesWritten() > FLUSH_BUFFER_SIZE) {

            if (callId != RecordingState.ROOT_CALL_RECORDING_ID) {
                this.recordedCalls = new SerializedRecordedMethodCallList(this.recordingId, pageAllocator);
            }

            agentDataWriter.write(typeResolver, recordingMetadata, recordedCalls);

            if (callId == RecordingState.ROOT_CALL_RECORDING_ID) {
                this.recordedCalls = null;
            }
        }
    }
}
