package com.ulyp.agent;

import javax.annotation.Nullable;

import com.ulyp.core.CallRecordBuffer;
import com.ulyp.core.RecordingMetadata;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RecordingState {

    public static boolean newFlowEnabled = false;

    private int recordingId;
    private long callId = 0;
    private boolean enabled;
    @Nullable
    private RecordingMetadata recordingMetadata;
    @Nullable
    private CallRecordBuffer callRecordBuffer;

    public long nextCallId() {
        return callId++;
    }

    public void setRecordingMetadata(@Nullable RecordingMetadata recordingMetadata) {
        if (recordingMetadata != null) {
            this.recordingMetadata = recordingMetadata;
            this.recordingId = recordingMetadata.getId();
        } else {
            this.recordingMetadata = null;
            this.recordingId = 0;
        }
    }
}
