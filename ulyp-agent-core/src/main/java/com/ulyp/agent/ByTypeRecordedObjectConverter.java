package com.ulyp.agent;

import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BufferBytesOut;
import com.ulyp.core.recorders.*;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.core.util.SystemPropertyUtil;
import lombok.extern.slf4j.Slf4j;
import org.agrona.concurrent.UnsafeBuffer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.concurrent.NotThreadSafe;


@Slf4j
@NotThreadSafe
public class ByTypeRecordedObjectConverter implements RecordedObjectConverter {

    private static final int TMP_BUFFER_SIZE = SystemPropertyUtil.getInt("ulyp.recording.tmp-buffer.size", 16 * 1024);

    private final TypeResolver typeResolver;
    private byte[] tmpBuffer;

    public ByTypeRecordedObjectConverter(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    @Override
    public Object[] prepare(Object[] args) {
        if (args == null) {
            return null;
        }
        for (int i = 0; i < args.length; i++) {
            args[i] = prepare(args[i]);
        }
        return args;
    }

    /**
     * Resolves type for an object, then checks if it can be recorded asynchronously in a background thread. Most objects
     * have only their identity hash code and type id recorded, so it can be safely done concurrently in some other thread.
     * Collections have a few of their items recorded (if enabled), so the recording must happen here.
     */
    public Object prepare(Object value) {
        if (value == null) {
            return null;
        }

        Type type = typeResolver.get(value);

        ObjectRecorder recorder = type.getRecorderHint();
        if (recorder == null) {
            recorder = RecorderChooser.getInstance().chooseForType(value.getClass());
            type.setRecorderHint(recorder);
        }

        if (recorder.supportsAsyncRecording()) {
            // recording will be done in the background thread
            return value;
        } else {
            return recordNow(value, recorder, type);
        }
    }

    private @NotNull Object recordNow(Object value, ObjectRecorder recorder, Type type) {
        BufferBytesOut output = new BufferBytesOut(new UnsafeBuffer(getTmpBuffer()));

        try {

            recorder.write(value, output, typeResolver);
            return new QueuedRecordedObject(type, recorder.getId(), output.copy());
        } catch (Exception e) {

            if (LoggingSettings.DEBUG_ENABLED) {
                log.debug("Error while recording object", e);
            }
            // recording failed, we can only record identity
            return new QueuedIdentityObject(type.getId(), value);
        }
    }

    private byte[] getTmpBuffer() {
        if (tmpBuffer == null) {
            tmpBuffer = new byte[TMP_BUFFER_SIZE];
        }
        return tmpBuffer;
    }
}
