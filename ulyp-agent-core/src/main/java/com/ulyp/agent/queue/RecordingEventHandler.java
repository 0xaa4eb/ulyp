package com.ulyp.agent.queue;

import com.ulyp.agent.RecordDataWriter;
import com.ulyp.core.CallRecordBuffer;
import com.ulyp.core.MethodRepository;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.TypeResolver;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RecordingEventHandler {

    private final TypeResolver typeResolver;
    private final RecordDataWriter recordDataWriter;
    private final MethodRepository methodRepository;
    private RecordingMetadata recordingMetadata;
    private CallRecordBuffer buffer;

    public RecordingEventHandler(TypeResolver typeResolver, RecordDataWriter recordDataWriter) {
        this.typeResolver = typeResolver;
        this.recordDataWriter = recordDataWriter;
        this.methodRepository = recordDataWriter.getMethodRepository();
    }

    void onRecordingMetadataUpdate(RecordingMetadataQueueEvent update) {
        recordingMetadata = update.getRecordingMetadata();
    }

    void onEnterCallRecord(EnterRecordQueueEvent enterRecord) {
        if (buffer == null) {
            buffer = new CallRecordBuffer(enterRecord.getRecordingId());
        }

        buffer.recordMethodEnter(
                typeResolver,
                /* TODO remove after advice split */methodRepository.get(enterRecord.getMethodId()).getId(),
                enterRecord.getCallee(),
                enterRecord.getArgs(),
                enterRecord.getNanoTime()
        );
    }

    void onExitCallRecord(ExitRecordQueueEvent exitRecord) {
        int recordingId = exitRecord.getRecordingId();
        CallRecordBuffer buffer = this.buffer;
        if (buffer == null) {
            log.debug("Call record buffer not found for recording id " + recordingId);
            return;
        }
        if (exitRecord.isThrown()) {
            buffer.recordMethodExit(typeResolver, null, (Throwable) exitRecord.getReturnValue(), exitRecord.getCallId(), exitRecord.getNanoTime());
        } else {
            buffer.recordMethodExit(typeResolver, exitRecord.getReturnValue(), null, exitRecord.getCallId(), exitRecord.getNanoTime());
        }

        if (buffer.isComplete() ||
            buffer.estimateBytesSize() > 32 * 1024 * 1024) {

            if (!buffer.isComplete()) {
                this.buffer = buffer.cloneWithoutData();
            }

            recordDataWriter.write(typeResolver, recordingMetadata, buffer);

            if (buffer.isComplete()) {
                this.buffer = null;
            }
        }
    }
}
