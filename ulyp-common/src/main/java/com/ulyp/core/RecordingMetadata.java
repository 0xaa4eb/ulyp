package com.ulyp.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ulyp.transport.BinaryRecordingMetadataDecoder;
import com.ulyp.transport.BinaryRecordingMetadataDecoder.StackTraceElementsDecoder;
import com.ulyp.transport.BinaryRecordingMetadataEncoder;
import com.ulyp.transport.BinaryRecordingMetadataEncoder.StackTraceElementsEncoder;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
        // other fields are needed for the first chunk only
        return RecordingMetadata.builder()
            .id(id)
            .logCreatedEpochMillis(System.currentTimeMillis())
            .build();
    }

    public static RecordingMetadata deserialize(BinaryRecordingMetadataDecoder decoder) {
        List<String> stackTraceElements = new ArrayList<>();
        for (StackTraceElementsDecoder stackTraceElement : decoder.stackTraceElements()) {
            stackTraceElements.add(stackTraceElement.value());
        }

        return RecordingMetadata.builder()
                .recordingStartedEpochMillis(decoder.recordingStartedEpochMillis())
                .logCreatedEpochMillis(decoder.logCreatedEpochMillis())
                .recordingCompletedEpochMillis(decoder.recordingCompletedEpochMillis())
                .id(decoder.recordingId())
                .threadId(decoder.threadId())
                .stackTraceElements(stackTraceElements)
                .threadName(decoder.threadName())
                .build();
    }

    public void serialize(BinaryRecordingMetadataEncoder encoder) {
        encoder.recordingStartedEpochMillis(recordingStartedEpochMillis);
        encoder.logCreatedEpochMillis(logCreatedEpochMillis);
        encoder.recordingCompletedEpochMillis(recordingCompletedEpochMillis);
        encoder.recordingId(id);
        encoder.threadId(threadId);
        StackTraceElementsEncoder stackTraceElementsEncoder = encoder.stackTraceElementsCount(stackTraceElements.size());
        for (String stackTraceElement : stackTraceElements) {
            StackTraceElementsEncoder elementEncoder = stackTraceElementsEncoder.next();
            elementEncoder.value(stackTraceElement);
        }
        encoder.threadName(threadName);
    }
}
