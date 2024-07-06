package com.ulyp.agent.queue;

import com.ulyp.agent.queue.events.RecordingEvent;
import com.ulyp.core.util.SystemPropertyUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * A batch of events which inside every cell of the recording queue disruptor. Recording threads gather some number of
 * events in thread local batch and add events until the batch is full.
 */
@Getter
public class RecordingEventBatch {

    private static final int BATCH_SIZE = SystemPropertyUtil.getInt("ulyp.recording-queue.batch-size", 256);

    @Setter
    private int recordingId;
    private List<RecordingEvent> events;

    public RecordingEventBatch() {
        this.events = null;
    }

    public void reset() {
        this.events = null;
    }

    public void resetForUpcomingEvents() {
        events = new ArrayList<>(BATCH_SIZE);
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    public boolean isFull() {
        return events.size() >= BATCH_SIZE;
    }

    public void add(RecordingEvent event) {
        this.events.add(event);
    }

    public void moveFrom(RecordingEventBatch other) {
        this.recordingId = other.recordingId;
        this.events = other.events;
    }
}
