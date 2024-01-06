package com.ulyp.agent.queue;

import com.ulyp.core.RecordingMetadata;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
class RecordingMetadataQueueEvent {

    private final RecordingMetadata recordingMetadata;
}
