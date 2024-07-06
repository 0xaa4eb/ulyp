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
    private long threadId;
    private String threadName;
    private long recordingStartedMillis;
    private long logCreatedEpochMillis; // TODO remove
    private long recordingFinishedMillis;
    @Builder.Default @ToString.Exclude
    private List<String> stackTraceElements = Collections.emptyList();

    public RecordingMetadata withCompleteTime(long recordingFinishedMillis) {
        return RecordingMetadata.builder()
            .id(id)
            .threadId(threadId)
            .threadName(threadName)
            .recordingStartedMillis(recordingStartedMillis)
            .recordingFinishedMillis(recordingFinishedMillis)
            .stackTraceElements(stackTraceElements)
            .build();
    }
}
