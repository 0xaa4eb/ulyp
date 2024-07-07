package com.ulyp.agent.queue;

import com.ulyp.agent.AgentDataWriter;
import com.ulyp.agent.queue.events.*;
import com.ulyp.core.CallRecordBuffer;
import com.ulyp.core.MethodRepository;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.TypeResolver;

import com.ulyp.core.mem.DirectBufMemPageAllocator;
import com.ulyp.core.mem.MemPageAllocator;
import com.ulyp.core.recorders.QueuedIdentityObject;
import com.ulyp.core.util.SystemPropertyUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RecordingEventProcessor {

    private static final int FLUSH_BUFFER_SIZE = SystemPropertyUtil.getInt("ulyp.recording-queue.serialization-buffer-size", 256 * 1024);

    private final TypeResolver typeResolver;
    private final AgentDataWriter agentDataWriter;
    private final MethodRepository methodRepository;
    private RecordingMetadata recordingMetadata;
    private CallRecordBuffer buffer;
    private MemPageAllocator pageAllocator;

    public RecordingEventProcessor(TypeResolver typeResolver, AgentDataWriter agentDataWriter) {
        this.typeResolver = typeResolver;
        this.agentDataWriter = agentDataWriter;
        this.methodRepository = agentDataWriter.getMethodRepository();
        this.pageAllocator = new DirectBufMemPageAllocator();
    }

    void onRecordingStarted(RecordingStartedEvent update) {
        recordingMetadata = update.getRecordingMetadata();
    }

    void onEnterCallRecord(int recordingId, EnterMethodRecordingEvent enterRecord) {
        if (buffer == null) {
            buffer = new CallRecordBuffer(recordingId, pageAllocator);
        }

        long nanoTime = (enterRecord instanceof TimestampedEnterMethodRecordingEvent) ? ((TimestampedEnterMethodRecordingEvent) enterRecord).getNanoTime() : -1;
        buffer.recordMethodEnter(
                enterRecord.getCallId(),
                typeResolver,
                /* TODO remove after advice split */methodRepository.get(enterRecord.getMethodId()).getId(),
                enterRecord.getCallee(),
                enterRecord.getArgs(),
                nanoTime
        );
    }

    public void onRecordingFinished(RecordingFinishedEvent event) {
        recordingMetadata = recordingMetadata.withCompleteTime(event.getFinishTimeMillis());
        agentDataWriter.write(typeResolver, recordingMetadata, this.buffer);
        this.buffer = null;
    }

    void onExitCallRecord(int recordingId, ExitMethodRecordingEvent exitRecord) {
        CallRecordBuffer currentBuffer = this.buffer;
        if (currentBuffer == null) {
            log.debug("Call record buffer not found for recording id " + recordingId);
            return;
        }
        long nanoTime = (exitRecord instanceof TimestampedExitMethodRecordingEvent) ? ((TimestampedExitMethodRecordingEvent) exitRecord).getNanoTime() : -1;
        if (exitRecord.isThrown()) {
            currentBuffer.recordMethodExit(typeResolver, null, (Throwable) exitRecord.getReturnValue(), exitRecord.getCallId(), nanoTime);
        } else {
            currentBuffer.recordMethodExit(typeResolver, exitRecord.getReturnValue(), null, exitRecord.getCallId(), nanoTime);
        }

        if (currentBuffer.estimateBytesSize() > FLUSH_BUFFER_SIZE) {

            if (!currentBuffer.isComplete()) {
                this.buffer = currentBuffer.cloneWithoutData();
            }

            agentDataWriter.write(typeResolver, recordingMetadata, currentBuffer);

            if (currentBuffer.isComplete()) {
                this.buffer = null;
            }
        }
    }
}
