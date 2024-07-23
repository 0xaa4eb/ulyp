package com.ulyp.agent;

import javax.annotation.Nullable;

import com.ulyp.core.RecordingMetadata;

import lombok.Getter;
import lombok.Setter;

/**
 * Single state for recording session. There is only single {@link ThreadLocal#get()} call for every recorded method
 */
@Getter
public class RecordingState {

    public static final int ROOT_CALL_RECORDING_ID = 1;

    @Getter
    private int recordingId = -1;
    @Nullable
    private RecordingMetadata recordingMetadata;
    private int callId = ROOT_CALL_RECORDING_ID;
    @Setter
    private boolean enabled;
    @Getter
    @Setter
    private RecordingEventBuffer eventBuffer;

    public RecordingState() {

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
