package com.ulyp.agent.queue.events;

import lombok.Getter;

@Getter
public abstract class AbstractEnterMethodRecordingEvent implements RecordingEvent {

    protected final int methodId;
    protected final Object callee;

    public AbstractEnterMethodRecordingEvent(int methodId, Object callee) {
        this.methodId = methodId;
        this.callee = callee;
    }
}
