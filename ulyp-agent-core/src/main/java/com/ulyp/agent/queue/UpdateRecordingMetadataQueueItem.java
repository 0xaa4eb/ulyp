package com.ulyp.agent.queue;

import com.ulyp.core.RecordingMetadata;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
class UpdateRecordingMetadataQueueItem {

    private final RecordingMetadata recordingMetadata;
}
