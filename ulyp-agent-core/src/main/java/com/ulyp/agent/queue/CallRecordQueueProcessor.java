package com.ulyp.agent.queue;

import java.util.HashMap;
import java.util.Map;

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

    public CallRecordQueueProcessor(TypeResolver typeResolver, RecordDataWriter recordDataWriter) {
        this.typeResolver = typeResolver;
        this.recordDataWriter = recordDataWriter;
    }

    @Override
    public void onEvent(CallRecordQueueItemHolder event, long sequence, boolean endOfBatch) {
        if (event.item instanceof EnterRecordQueueItem) {
            EnterRecordQueueItem enterRecord = (EnterRecordQueueItem) event.item;

            CallRecordBuffer callRecordBuffer = buffers.get(enterRecord.getRecordingMetadata().getId());
            if (callRecordBuffer == null) {
                callRecordBuffer = new CallRecordBuffer(enterRecord.getRecordingMetadata().getId());
                recordingMetadataMap.put(enterRecord.getRecordingMetadata().getId(), enterRecord.getRecordingMetadata());
                buffers.put(enterRecord.getRecordingMetadata().getId(), callRecordBuffer);
            }

            callRecordBuffer.recordMethodEnter(typeResolver, enterRecord.getMethodId(), enterRecord.getCallee(), enterRecord.getArgs());
        } else {
            ExitRecordQueueItem exitRecord = (ExitRecordQueueItem) event.item;
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
    }
}
