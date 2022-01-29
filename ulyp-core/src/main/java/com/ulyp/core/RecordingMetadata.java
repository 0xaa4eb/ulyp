package com.ulyp.core;

import com.ulyp.transport.BinaryRecordingMetadataDecoder;
import com.ulyp.transport.BinaryRecordingMetadataEncoder;
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

    public void serialize(BinaryRecordingMetadataEncoder encoder) {
        encoder.recordingStartedEpochMillis(recordingStartedEpochMillis);
        encoder.logCreatedEpochMillis(logCreatedEpochMillis);
        encoder.recordingCompletedEpochMillis(recordingCompletedEpochMillis);
        encoder.recordingId(id);
        encoder.threadId(threadId);
        encoder.threadName(threadName);
    }

    public static RecordingMetadata deserialize(BinaryRecordingMetadataDecoder decoder) {
        return RecordingMetadata.builder()
                .recordingStartedEpochMillis(decoder.recordingStartedEpochMillis())
                .logCreatedEpochMillis(decoder.logCreatedEpochMillis())
                .recordingCompletedEpochMillis(decoder.recordingCompletedEpochMillis())
                .id(decoder.recordingId())
                .threadId(decoder.threadId())
                .threadName(decoder.threadName())
                .build();
    }
}
