package com.ulyp.agent.queue.events;

import com.ulyp.core.RecordingMetadata;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecordingStartedEvent implements RecordingEvent {

    private final RecordingMetadata recordingMetadata;
}
