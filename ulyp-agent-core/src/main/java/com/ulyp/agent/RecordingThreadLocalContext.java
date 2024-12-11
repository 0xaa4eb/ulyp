package com.ulyp.agent;

import com.ulyp.agent.options.AgentOptions;
import com.ulyp.agent.util.ConstructedTypesStack;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.collections.CollectionsRecordingMode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

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
    private final ConstructedTypesStack constructingTypes;
    @Getter
    private int recordingId = -1;
    @Nullable
    private RecordingMetadata recordingMetadata;
    @Getter
    private final ObjectRecordingConverter recordingObjectConverter;
    private int callId = ROOT_CALL_RECORDING_ID;
    @Setter
    private boolean enabled;
    @Getter
    @Setter
    private RecordingEventBuffer eventBuffer;


    public RecordingThreadLocalContext(AgentOptions options, TypeResolver typeResolver) {
        this.recordingObjectConverter = initializeObjectRecordingConverter(options, typeResolver);
        this.constructingTypes = new ConstructedTypesStack();
    }

    private static @NotNull ObjectRecordingConverter initializeObjectRecordingConverter(AgentOptions options, TypeResolver typeResolver) {
        ObjectRecordingConverter converter;
        if (CollectionsRecordingMode.isDisabled(options.getCollectionsRecordingMode().get()) && !options.getArraysRecordingOption().get()) {
            converter = PassByRefObjectRecordingConverter.INSTANCE;
        } else {
            converter = new ByTypeObjectRecordingConverter(typeResolver);
        }
        if (options.isInstrumentConstructorsEnabled()) {
            converter = new ConstructorTrackingObjectConverter(typeResolver, converter);
        }
        return converter;
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
