package com.ulyp.agent.queue.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RecordingFinishedEvent implements RecordingEvent {

    private final long finishTimeMillis;
}
