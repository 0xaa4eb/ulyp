package com.ulyp.core.serializers;

import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.bytes.BinaryInput;
import com.ulyp.core.bytes.BinaryOutput;

import java.util.ArrayList;
import java.util.List;

public class RecordingMetadataSerializer implements Serializer<RecordingMetadata> {

    public static final RecordingMetadataSerializer instance = new RecordingMetadataSerializer();

    @Override
    public RecordingMetadata deserialize(BinaryInput input) {
        int recordingId = input.readInt();
        long threadId = input.readLong();
        long recordingStartedEpochMillis = input.readLong();
        long logCreatedEpochMillis = input.readLong();
        long recordingCompletedEpochMillis = input.readLong();
        String threadName = input.readString();

        int stackTraceElementsCount = input.readInt();
        List<String> stackTraceElements = new ArrayList<>(stackTraceElementsCount);
        for (int i = 0; i < stackTraceElementsCount; i++) {
            stackTraceElements.add(input.readString());
        }

        return RecordingMetadata.builder()
                .recordingStartedEpochMillis(recordingStartedEpochMillis)
                .logCreatedEpochMillis(logCreatedEpochMillis)
                .recordingCompletedEpochMillis(recordingCompletedEpochMillis)
                .id(recordingId)
                .threadId(threadId)
                .stackTraceElements(stackTraceElements)
                .threadName(threadName)
                .build();
    }

    @Override
    public void serialize(BinaryOutput out, RecordingMetadata recordingMetadata) {
        out.write(recordingMetadata.getId());
        out.write(recordingMetadata.getThreadId());
        out.write(recordingMetadata.getRecordingStartedEpochMillis());
        out.write(recordingMetadata.getLogCreatedEpochMillis());
        out.write(recordingMetadata.getRecordingCompletedEpochMillis());
        out.write(recordingMetadata.getThreadName());
        out.write(recordingMetadata.getStackTraceElements().size());
        for (String stackTraceElement: recordingMetadata.getStackTraceElements()) {
            out.write(stackTraceElement);
        }
    }
}
