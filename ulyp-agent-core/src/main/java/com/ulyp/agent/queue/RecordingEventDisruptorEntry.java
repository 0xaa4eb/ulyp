package com.ulyp.agent.queue;

import com.ulyp.agent.RecordingEventBuffer;
import com.ulyp.agent.queue.events.RecordingEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * A batch of events which inside every cell of the recording queue disruptor. Recording threads gather some number of
 * events in thread local batch and add events until the batch is full.
 */
@Getter
public class RecordingEventDisruptorEntry {

    @Setter
    private int recordingId;
    private List<RecordingEvent> events;

    public void reset() {
        this.events = null;
    }

    public void moveFrom(RecordingEventBuffer batch) {
        this.recordingId = batch.getRecordingId();
        this.events = batch.getEvents();
    }
}
