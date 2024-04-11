package com.ulyp.agent.queue;

import com.ulyp.agent.AgentDataWriter;
import com.ulyp.agent.queue.events.EnterRecordQueueEvent;
import com.ulyp.agent.queue.events.ExitRecordQueueEvent;
import com.ulyp.agent.queue.events.TimestampedEnterRecordQueueEvent;
import com.ulyp.agent.queue.events.TimestampedExitRecordQueueEvent;
import com.ulyp.core.CallRecordBuffer;
import com.ulyp.core.MethodRepository;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.TypeResolver;

import com.ulyp.core.mem.MemPageAllocator;
import com.ulyp.core.recorders.QueuedIdentityObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RecordingEventProcessor {

    private final TypeResolver typeResolver;
    private final AgentDataWriter agentDataWriter;
    private final MethodRepository methodRepository;
    private RecordingMetadata recordingMetadata;
    private CallRecordBuffer buffer;
    @Getter
    private boolean complete = false;
    private MemPageAllocator pageAllocator;
    private QueuedIdentityObject cachedQueuedIdentityCallee = new QueuedIdentityObject();

    public RecordingEventProcessor(TypeResolver typeResolver, AgentDataWriter agentDataWriter) {
        this.typeResolver = typeResolver;
        this.agentDataWriter = agentDataWriter;
        this.methodRepository = agentDataWriter.getMethodRepository();
        this.pageAllocator = new DirectBufMemPageAllocator();
    }

    void onRecordingMetadataUpdate(RecordingMetadataQueueEvent update) {
        recordingMetadata = update.getRecordingMetadata();
    }

    void onEventBatchStart() {
        // not used yet
    }

    void onEventBatchEnd() {
        // not used yet
    }

    void onEnterCallRecord(EnterRecordQueueEvent enterRecord) {
        if (buffer == null) {
            buffer = new CallRecordBuffer(enterRecord.getRecordingId(), pageAllocator);
        }
        cachedQueuedIdentityCallee.setTypeId(enterRecord.getCalleeTypeId());
        cachedQueuedIdentityCallee.setIdentityHashCode(enterRecord.getCalleeIdentityHashCode());

        long nanoTime = (enterRecord instanceof TimestampedEnterRecordQueueEvent) ? ((TimestampedEnterRecordQueueEvent) enterRecord).getNanoTime() : -1;
        buffer.recordMethodEnter(
                enterRecord.getCallId(),
                typeResolver,
                enterRecord.getMethodId(),
                cachedQueuedIdentityCallee,
                enterRecord.getArgs(),
                nanoTime
        );
    }

    void onExitCallRecord(ExitRecordQueueEvent exitRecord) {
        int recordingId = exitRecord.getRecordingId();
        CallRecordBuffer currentBuffer = this.buffer;
        if (currentBuffer == null) {
            log.debug("Call record buffer not found for recording id " + recordingId);
            return;
        }
        long nanoTime = (exitRecord instanceof TimestampedExitRecordQueueEvent) ? ((TimestampedExitRecordQueueEvent) exitRecord).getNanoTime() : -1;
        if (exitRecord.isThrown()) {
            currentBuffer.recordMethodExit(typeResolver, null, (Throwable) exitRecord.getReturnValue(), exitRecord.getCallId(), nanoTime);
        } else {
            currentBuffer.recordMethodExit(typeResolver, exitRecord.getReturnValue(), null, exitRecord.getCallId(), nanoTime);
        }

        if (currentBuffer.isComplete() || currentBuffer.estimateBytesSize() > 32 * 1024 * 1024) {

            if (!currentBuffer.isComplete()) {
                this.buffer = currentBuffer.cloneWithoutData();
            }

            agentDataWriter.write(typeResolver, recordingMetadata, currentBuffer);

            if (currentBuffer.isComplete()) {
                this.buffer = null;
                this.complete = true;
            }
        }
    }
}
