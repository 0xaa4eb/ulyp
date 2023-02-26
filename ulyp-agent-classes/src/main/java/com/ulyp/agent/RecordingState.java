package com.ulyp.agent;

import javax.annotation.Nullable;

import com.ulyp.core.CallRecordBuffer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class RecordingState {

    private boolean enabled;
/*
    @Nullable
    private RecordingMetadata recordingMetadata;
*/
    @Nullable
    private CallRecordBuffer callRecordBuffer;

    public RecordingState(CallRecordBuffer callRecordBuffer) {
        this.callRecordBuffer = callRecordBuffer;
        this.enabled = true;
    }
}
