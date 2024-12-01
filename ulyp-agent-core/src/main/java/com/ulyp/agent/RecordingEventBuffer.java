package com.ulyp.agent;

import com.ulyp.agent.queue.events.*;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.util.SystemPropertyUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread-local buffer for recording events. Client app threads gather some number of
 * events in into such buffers and post them to the background thread.
 * Not thread-safe. Every thread has its own event buffer.
 */
@Getter
@Slf4j
@NotThreadSafe
public class RecordingEventBuffer {

    private static final int MAX_BUFFER_SIZE = SystemPropertyUtil.getInt("ulyp.recording.max-buffer-size", 256);

    @Getter
    private final int recordingId;
    @Getter
    private List<RecordingEvent> events;

    public RecordingEventBuffer(int recordingId) {
        this.recordingId = recordingId;
        this.events = new ArrayList<>(MAX_BUFFER_SIZE);
    }

    public void reset() {
        events = new ArrayList<>(MAX_BUFFER_SIZE);
    }

    public boolean isFull() {
        return events.size() >= MAX_BUFFER_SIZE;
    }

    public void add(RecordingEvent event) {
        this.events.add(event);
    }

    public void appendRecordingStartedEvent(RecordingMetadata recordingMetadata) {
        add(new RecordingStartedEvent(recordingMetadata));
    }

    public void appendRecordingFinishedEvent(long recordingFinishedTimeMillis) {
        add(new RecordingFinishedEvent(recordingFinishedTimeMillis));
    }

    public void appendMethodEnterEvent(int methodId, @Nullable Object callee, Object[] args) {
        events.add(new EnterMethodRecordingEvent(methodId, callee, args));
    }

    public void appendMethodEnterEvent(int methodId, @Nullable Object callee, Object arg) {
        events.add(new EnterMethodOneArgRecordingEvent(methodId, callee, arg));
    }

    public void appendMethodEnterEvent(int methodId, @Nullable Object callee, Object arg1, Object arg2) {
        events.add(new EnterMethodTwoArgsRecordingEvent(methodId, callee, arg1, arg2));
    }

    public void appendMethodEnterEvent(int methodId, @Nullable Object callee, Object arg1, Object arg2, Object arg3) {
        events.add(new EnterMethodThreeArgsRecordingEvent(methodId, callee, arg1, arg2, arg3));
    }

    public void appendMethodEnterEvent(int methodId, @Nullable Object callee) {
        events.add(new EnterMethodNoArgsRecordingEvent(methodId, callee));
    }

    public void appendMethodEnterEvent(int methodId, @Nullable Object callee, long nanoTime) {
        events.add(new TimestampedEnterMethodNoArgsRecordingEvent(methodId, callee, nanoTime));
    }

    public void appendMethodEnterEvent(int methodId, @Nullable Object callee, Object arg, long nanoTime) {
        events.add(new TimestampedEnterMethodOneArgRecordingEvent(methodId, callee, arg, nanoTime));
    }

    public void appendMethodEnterEvent(int methodId, @Nullable Object callee, Object arg1, Object arg2, long nanoTime) {
        events.add(new TimestampedEnterMethodTwoArgsRecordingEvent(methodId, callee, arg1, arg2, nanoTime));
    }

    public void appendMethodEnterEvent(int methodId, @Nullable Object callee, Object arg1, Object arg2, Object arg3, long nanoTime) {
        events.add(new TimestampedEnterMethodThreeArgsRecordingEvent(methodId, callee, arg1, arg2, arg3, nanoTime));
    }

    public void appendMethodEnterEvent(int methodId, @Nullable Object callee, Object[] args, long nanoTime) {
        events.add(new TimestampedEnterMethodRecordingEvent(methodId, callee, args, nanoTime));
    }

    public void appendMethodExitEvent(int callId, Object returnValue, boolean thrown) {
        events.add(new ExitMethodRecordingEvent(callId, returnValue, thrown));
    }

    public void appendMethodExitEvent(int callId, Object returnValue, boolean thrown, long nanoTime) {
        events.add(new TimestampedExitMethodRecordingEvent(callId, returnValue, thrown, nanoTime));
    }
}
