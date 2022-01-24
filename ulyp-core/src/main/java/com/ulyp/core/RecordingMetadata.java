package com.ulyp.core;

import com.ulyp.transport.BinaryRecordingMetadataDecoder;
import com.ulyp.transport.BinaryRecordingMetadataEncoder;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class RecordingMetadata {

    public static final int WIRE_ID = 7;

    private final int id;
    private final long createEpochMillis;
    // TODO lifetimeMillis can't be here
    private final long lifetimeMillis;
    private final String threadName;
    private final long threadId;

    public void serialize(BinaryRecordingMetadataEncoder encoder) {
        encoder.createEpochMillis(createEpochMillis);
        encoder.lifetimeMillis(lifetimeMillis);
        encoder.recordingId(id);
        encoder.threadId(threadId);
        encoder.threadName(threadName);
    }

    public static RecordingMetadata deserialize(BinaryRecordingMetadataDecoder decoder) {
        return RecordingMetadata.builder()
                .createEpochMillis(decoder.createEpochMillis())
                .lifetimeMillis(decoder.lifetimeMillis())
                .id(decoder.recordingId())
                .threadId(decoder.threadId())
                .threadName(decoder.threadName())
                .build();
    }
}
