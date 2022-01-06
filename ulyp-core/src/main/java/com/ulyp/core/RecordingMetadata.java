package com.ulyp.core;

import com.ulyp.transport.BinaryRecordingMetadataDecoder;
import com.ulyp.transport.BinaryRecordingMetadataEncoder;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RecordingMetadata {

    private final long createEpochMillis;
    private final long lifetimeMillis;
    private final long recordingId;
    private final String threadName;
    private final long threadId;

    public void serialize(BinaryRecordingMetadataEncoder encoder) {
        encoder.createEpochMillis(createEpochMillis);
        encoder.lifetimeMillis(lifetimeMillis);
        encoder.recordingId(recordingId);
        encoder.threadId(threadId);
        encoder.threadName(threadName);
    }

    public static RecordingMetadata deserialize(BinaryRecordingMetadataDecoder decoder) {
        return RecordingMetadata.builder()
                .createEpochMillis(decoder.createEpochMillis())
                .lifetimeMillis(decoder.lifetimeMillis())
                .recordingId(decoder.recordingId())
                .threadId(decoder.threadId())
                .threadName(decoder.threadName())
                .build();
    }
}
