package com.ulyp.core;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

@Builder
@Getter
@ToString
@Setter
public class RecordingMetadata {

    public static final int WIRE_ID = 7;

    private int id;
    private long recordingStartedEpochMillis;
    private long logCreatedEpochMillis;
    private long recordingCompletedEpochMillis;
    private String threadName;
    private long threadId;
    @Builder.Default
    private List<String> stackTraceElements = Collections.emptyList();

    public RecordingMetadata withNewCreationTimestamp() {
        return RecordingMetadata.builder()
            .id(id)
            .recordingStartedEpochMillis(recordingStartedEpochMillis)
            .logCreatedEpochMillis(System.currentTimeMillis())
            .build();
    }
}
