package com.ulyp.agent;

import com.ulyp.agent.util.EnhancedThreadLocal;
import com.ulyp.agent.policy.StartRecordingPolicy;
import com.ulyp.core.*;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.storage.StorageWriter;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.TestOnly;

@SuppressWarnings("unused")
@Slf4j
@ThreadSafe
public class Recorder {

    /**
    * Keeps current active recording session count. Based on the fact that most of the time there is no
    * recording sessions and this counter is equal to 0, it's possible to make a small performance optimization.
    * Advice code (see com.ulyp.agent.MethodCallRecordingAdvice) can first check if there are any recording sessions are active at all.
    * If there are any, then advice code will check thread local and know if there is recording session in this thread precisely.
    * This helps minimizing unneeded thread local lookups in the advice code
    */
    public static final AtomicInteger currentRecordingSessionCount = new AtomicInteger();

    private final EnhancedThreadLocal<RecordingState> threadLocalRecordingState = new EnhancedThreadLocal<>();
    private final CallIdGenerator initialCallIdGenerator;
    private final StartRecordingPolicy startRecordingPolicy;
    private final RecordDataWriter recordDataWriter;

    public Recorder(CallIdGenerator callIdGenerator, StartRecordingPolicy startRecordingPolicy, StorageWriter storageWriter) {
        this.recordDataWriter = new RecordDataWriter(storageWriter);
        this.startRecordingPolicy = startRecordingPolicy;
        this.initialCallIdGenerator = callIdGenerator;
    }

    public boolean recordingIsActiveInCurrentThread() {
        RecordingState recordingState = threadLocalRecordingState.get();
        return recordingState != null && recordingState.isEnabled();
    }

    /**
     * Allows disabling recording temporary so that no recording is done. Currently, is only used in logging
     * facilities in order to avoid unneeded recording calls while logging.
     * Works along with {@link StartRecordingPolicy} but those functionalities are used for different purposes
     */
    public void disableRecording() {
        RecordingState recordingState = threadLocalRecordingState.get();
        if (recordingState != null) {
            recordingState.setEnabled(false);
        } else {
            recordingState = new RecordingState();
            recordingState.setEnabled(false);
            threadLocalRecordingState.set(recordingState);
        }
    }

    public void enableRecording() {
        RecordingState recordingState = threadLocalRecordingState.get();
        if (recordingState != null) {
            if (recordingState.getCallRecordBuffer() != null) {
                recordingState.setEnabled(true);
            } else {
                threadLocalRecordingState.clear();
            }
        }
    }

    public long startOrContinueRecordingOnMethodEnter(TypeResolver typeResolver, Method method, @Nullable Object callee, Object[] args) {
        if (startRecordingPolicy.canStartRecording()) {
            RecordingState recordingState = threadLocalRecordingState.computeIfAbsent(() -> {
                CallRecordBuffer newCallRecordBuffer = new CallRecordBuffer(typeResolver, initialCallIdGenerator.getNextStartValue());
                currentRecordingSessionCount.incrementAndGet();
                if (LoggingSettings.INFO_ENABLED) {
                    log.info("Started recording {} at method {}", newCallRecordBuffer.getRecordingMetadata().getId(), method.toShortString());
                }
                return new RecordingState(newCallRecordBuffer);
            });

            return onMethodEnter(recordingState, method, callee, args);
        } else {
            return -1;
        }
    }

    public long startOrContinueRecordingOnConstructorEnter(TypeResolver typeResolver, Method method, Object[] args) {
        if (startRecordingPolicy.canStartRecording()) {
            RecordingState recordingState = threadLocalRecordingState.computeIfAbsent(() -> {
                CallRecordBuffer newCallRecordBuffer = new CallRecordBuffer(typeResolver, initialCallIdGenerator.getNextStartValue());
                currentRecordingSessionCount.incrementAndGet();
                if (LoggingSettings.INFO_ENABLED) {
                    log.info("Started recording {} at method {}", newCallRecordBuffer.getRecordingMetadata().getId(), method.toShortString());
                }
                return new RecordingState(newCallRecordBuffer);
            });

            return onConstructorEnter(recordingState, method, args);
        } else {
            return -1;
        }
    }

    public long onConstructorEnter(Method method, Object[] args) {
        return onMethodEnter(threadLocalRecordingState.get(), method, null, args);
    }

    public long onConstructorEnter(RecordingState recordingState, Method method, Object[] args) {
        return onMethodEnter(recordingState, method, null, args);
    }

    public long onMethodEnter(Method method, @Nullable Object callee, Object[] args) {
        return onMethodEnter(threadLocalRecordingState.get(), method, callee, args);
    }

    public long onMethodEnter(RecordingState recordingState, Method method, @Nullable Object callee, Object[] args) {
        try {
            if (recordingState == null || !recordingState.isEnabled()) {
                return -1;
            }
            CallRecordBuffer callRecordBuffer = recordingState.getCallRecordBuffer();
            if (callRecordBuffer == null) {
                return -1;
            }

            try {
                recordingState.setEnabled(false);
                return callRecordBuffer.onMethodEnter(method, callee, args);
            } finally {
                recordingState.setEnabled(true);
            }
        } catch (Throwable err) {
            log.error("Error happened when recording", err);
            return -1;
        }
    }

    public void onConstructorExit(TypeResolver typeResolver, Method method, Object result, long callId) {
        onMethodExit(typeResolver, method, result, null, callId);
    }

    public void onMethodExit(TypeResolver typeResolver, Method method, Object result, Throwable thrown, long callId) {
        try {
            RecordingState recordingState = threadLocalRecordingState.get();
            if (recordingState == null || !recordingState.isEnabled()) return;
            CallRecordBuffer callRecords = recordingState.getCallRecordBuffer();
            if (callRecords == null) return;

            try {
                recordingState.setEnabled(false);
                callRecords.onMethodExit(method, result, thrown, callId);
            } finally {
                recordingState.setEnabled(true);
            }

            if (callRecords.isComplete() ||
                    callRecords.estimateBytesSize() > 32 * 1024 * 1024 ||
                    (
                            (System.currentTimeMillis() - callRecords.getRecordingMetadata().getLogCreatedEpochMillis()) > 100
                                    &&
                                    callRecords.getRecordedCallsSize() > 0
                    )) {
                CallRecordBuffer newBuffer = callRecords.cloneWithoutData();

                if (!callRecords.isComplete()) {
                    recordingState.setCallRecordBuffer(newBuffer);
                } else {
                    threadLocalRecordingState.clear();
                    currentRecordingSessionCount.decrementAndGet();
                    if (LoggingSettings.INFO_ENABLED) {
                        log.info("Finished recording {} at method {}, recorded {} calls", callRecords.getRecordingMetadata().getId(), method.toShortString(), callRecords.getTotalRecordedEnterCalls());
                    }
                }

                recordDataWriter.write(typeResolver, callRecords);
            }
        } catch (Throwable err) {
            log.error("Error happened when recording", err);
        }
    }

    @TestOnly
    RecordingState getRecordingState() {
        return threadLocalRecordingState.get();
    }
}
