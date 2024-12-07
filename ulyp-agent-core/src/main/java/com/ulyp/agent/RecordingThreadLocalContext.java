package com.ulyp.agent;

import com.ulyp.agent.options.AgentOptions;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.collections.CollectionsRecordingMode;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

/**
 * Thread-local context which contains all necessary information about ongoing (if any) recording session.
 *
 * There is only single {@link ThreadLocal#get()} call for every recorded method
 */
@Getter
public class RecordingThreadLocalContext {

    public static final int ROOT_CALL_RECORDING_ID = 1;

    @Getter
    private int recordingId = -1;
    @Nullable
    private RecordingMetadata recordingMetadata;
    @Getter
    private RecordedObjectConverter objectConverter;
    private int callId = ROOT_CALL_RECORDING_ID;
    @Setter
    private boolean enabled;
    @Getter
    @Setter
    private RecordingEventBuffer eventBuffer;

    public RecordingThreadLocalContext(AgentOptions options, TypeResolver typeResolver) {
        if (CollectionsRecordingMode.isDisabled(options.getCollectionsRecordingMode().get()) && !options.getArraysRecordingOption().get()) {
            this.objectConverter = PassByRefRecordedObjectConverter.INSTANCE;
        } else {
            this.objectConverter = new ByTypeRecordedObjectConverter(typeResolver);
        }
    }

    public int nextCallId() {
        return callId++;
    }

    public void setRecordingMetadata(@Nullable RecordingMetadata recordingMetadata) {
        if (recordingMetadata != null) {
            this.recordingMetadata = recordingMetadata;
            this.recordingId = recordingMetadata.getId();
        } else {
            this.recordingMetadata = null;
            this.recordingId = -1;
        }
        this.callId = ROOT_CALL_RECORDING_ID;
    }
}
