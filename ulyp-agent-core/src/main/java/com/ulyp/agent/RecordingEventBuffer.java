package com.ulyp.agent;

import com.ulyp.agent.queue.events.*;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BufferBytesOut;
import com.ulyp.core.recorders.*;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.core.util.SystemPropertyUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.agrona.concurrent.UnsafeBuffer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread-local buffer for recording events. Recording threads gather some number of
 * events in into such buffers and post them to the background thread.
 */
@Getter
@Slf4j
public class RecordingEventBuffer {

    private static final int MAX_BUFFER_SIZE = SystemPropertyUtil.getInt("ulyp.recording.max-buffer-size", 256);
    private static final int TMP_BUFFER_SIZE = SystemPropertyUtil.getInt("ulyp.recording.tmp-buffer.size", 16 * 1024);

    @Getter
    private final int recordingId;
    private final boolean performanceMode;
    private final TypeResolver typeResolver;
    @Getter
    private List<RecordingEvent> events;
    private byte[] tmpBuffer;

    public RecordingEventBuffer(int recordingId, Settings settings, TypeResolver typeResolver) {
        this.recordingId = recordingId;
        this.performanceMode = settings.isPerformanceModeEnabled();
        this.typeResolver = typeResolver;
        this.events = new ArrayList<>(MAX_BUFFER_SIZE);
    }

    public void reset() {
        events = new ArrayList<>(MAX_BUFFER_SIZE);
    }

    public boolean isEmpty() {
        return events.isEmpty();
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
        Object[] argsPrepared = prepareArgs(args);

        events.add(new EnterMethodRecordingEvent(methodId, callee, argsPrepared));
    }

    public void appendMethodEnterEvent(int methodId, @Nullable Object callee, Object arg) {
        events.add(new EnterMethodOneArgRecordingEvent(methodId, callee, prepareArg(arg)));
    }

    public void appendMethodEnterEvent(int methodId, @Nullable Object callee) {
        events.add(new EnterMethodNoArgsRecordingEvent(methodId, callee));
    }

    public void appendMethodEnterEvent(int methodId, @Nullable Object callee, long nanoTime) {
        events.add(new TimestampedEnterMethodNoArgsRecordingEvent(methodId, callee, nanoTime));
    }

    public void appendMethodEnterEvent(int methodId, @Nullable Object callee, Object arg, long nanoTime) {
        events.add(new TimestampedEnterMethodOneArgRecordingEvent(methodId, callee, prepareArg(arg), nanoTime));
    }

    public void appendMethodEnterEvent(int methodId, @Nullable Object callee, Object[] args, long nanoTime) {
        Object[] argsPrepared = prepareArgs(args);

        events.add(new TimestampedEnterMethodRecordingEvent(methodId, callee, argsPrepared, nanoTime));
    }

    public void appendMethodExitEvent(int callId, Object returnValue, boolean thrown) {
        Object returnValuePrepared = prepareReturnValue(returnValue);
        events.add(new ExitMethodRecordingEvent(callId, returnValuePrepared, thrown));
    }

    public void appendMethodExitEvent(int callId, Object returnValue, boolean thrown, long nanoTime) {
        Object returnValuePrepared = prepareReturnValue(returnValue);
        events.add(new TimestampedExitMethodRecordingEvent(callId, returnValuePrepared, thrown, nanoTime));
    }

    private Object prepareReturnValue(Object returnValue) {
        Object returnValuePrepared;
        if (performanceMode) {
            returnValuePrepared = returnValue;
        } else {
            returnValuePrepared = convert(returnValue);
        }
        return returnValuePrepared;
    }

    private Object prepareArg(Object arg) {
        if (performanceMode) {
            return arg;
        } else {
            return convert(arg);
        }
    }

    private Object[] prepareArgs(Object[] args) {
        if (args == null) {
            return null;
        }
        Object[] argsPrepared;
        if (performanceMode) {
            argsPrepared = args;
        } else {
            argsPrepared = convert(args);
        }
        return argsPrepared;
    }

    private Object[] convert(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = convert(args[i]);
        }
        return args;
    }

    /**
     * Resolves type for an object, then checks if it can be recorded asynchronously in a background thread. Most objects
     * have only their identity hash code and type id recorded, so it can be safely done concurrently in some other thread.
     * Collections have a few of their items recorded (if enabled), so the recording must happen here.
     */
    private Object convert(Object value) {
        Type type = typeResolver.get(value);
        ObjectRecorder recorder = type.getRecorderHint();
        if (value != null && recorder == null) {
            recorder = RecorderChooser.getInstance().chooseForType(value.getClass());
            type.setRecorderHint(recorder);
        }
        if (value == null || recorder.supportsAsyncRecording()) {
            if (value != null && recorder instanceof IdentityRecorder) {
                return new QueuedIdentityObject(type.getId(), value);
            } else {
                return value;
            }
        } else {
            BufferBytesOut output = new BufferBytesOut(new UnsafeBuffer(getTmpBuffer()));
            try {
                recorder.write(value, output, typeResolver);
                return new QueuedRecordedObject(type, recorder.getId(), output.copy());
            } catch (Exception e) {
                if (LoggingSettings.DEBUG_ENABLED) {
                    log.debug("Error while recording object", e);
                }
                return new QueuedIdentityObject(type.getId(), value);
            }
        }
    }

    private byte[] getTmpBuffer() {
        if (tmpBuffer != null) {
            return tmpBuffer;
        } else {
            tmpBuffer = new byte[TMP_BUFFER_SIZE];
            return tmpBuffer;
        }
    }
}
