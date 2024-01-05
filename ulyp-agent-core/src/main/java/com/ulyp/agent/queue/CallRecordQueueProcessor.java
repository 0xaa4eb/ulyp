package com.ulyp.agent.queue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.lmax.disruptor.EventHandler;
import com.ulyp.agent.RecordDataWriter;
import com.ulyp.core.CallRecordBuffer;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.TypeResolver;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CallRecordQueueProcessor implements EventHandler<CallRecordQueueItemHolder> {

    private final TypeResolver typeResolver;
    private final RecordDataWriter recordDataWriter;
    private final Map<Integer, RecordingMetadata> recordingMetadataMap = new HashMap<>();
    private final Map<Integer, CallRecordBuffer> buffers = new HashMap<>();
    private final AtomicLong lastProcessedSeq = new AtomicLong();

    public CallRecordQueueProcessor(TypeResolver typeResolver, RecordDataWriter recordDataWriter) {
        this.typeResolver = typeResolver;
        this.recordDataWriter = recordDataWriter;
    }

    @Override
    public void onEvent(CallRecordQueueItemHolder event, long sequence, boolean endOfBatch) {
        if (event.item instanceof EnterRecordQueueItem) {
            onEnterCallRecord((EnterRecordQueueItem) event.item);
        } else if (event.item instanceof ExitRecordQueueItem) {
            onExitCallRecord((ExitRecordQueueItem) event.item);
        } else {
            onRecordingMetadataUpdate((UpdateRecordingMetadataQueueItem) event.item);
        }

        lastProcessedSeq.lazySet(sequence);
    }

    private void onRecordingMetadataUpdate(UpdateRecordingMetadataQueueItem update) {
        recordingMetadataMap.put(update.getRecordingMetadata().getId(), update.getRecordingMetadata());
    }

    private void onEnterCallRecord(EnterRecordQueueItem enterRecord) {
        CallRecordBuffer callRecordBuffer = buffers.get(enterRecord.getRecordingId());
        if (callRecordBuffer == null) {
            callRecordBuffer = new CallRecordBuffer(enterRecord.getRecordingId());
            buffers.put(enterRecord.getRecordingId(), callRecordBuffer);
        }

        callRecordBuffer.recordMethodEnter(typeResolver, enterRecord.getMethodId(), enterRecord.getCallee(), enterRecord.getArgs());
    }

    private void onExitCallRecord(ExitRecordQueueItem exitRecord) {
        int recordingId = exitRecord.getRecordingId();
        CallRecordBuffer buffer = buffers.get(recordingId);
        if (buffer == null) {
            log.debug("Call record buffer not found for recording id " + recordingId);
            return;
        }
        if (exitRecord.isThrown()) {
            buffer.recordMethodExit(typeResolver, null, (Throwable) exitRecord.getReturnValue(), exitRecord.getCallId());
        } else {
            buffer.recordMethodExit(typeResolver, exitRecord.getReturnValue(), null, exitRecord.getCallId());
        }

        if (buffer.isComplete() ||
            buffer.estimateBytesSize() > 32 * 1024 * 1024) {

            if (!buffer.isComplete()) {
                CallRecordBuffer newBuffer = buffer.cloneWithoutData();
                buffers.put(recordingId, newBuffer);
            }

            recordDataWriter.write(typeResolver, recordingMetadataMap.get(recordingId), buffer);

            if (buffer.isComplete()) {
                buffers.remove(recordingId);
            }
        }
    }

    public long getLastProcessedSeq() {
        return lastProcessedSeq.get();
    }
}
