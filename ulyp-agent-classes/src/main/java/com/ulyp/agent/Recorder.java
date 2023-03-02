package com.ulyp.agent;

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

    private final MethodRepository methodRepository;
    private final TypeResolver typeResolver;
    private final ThreadLocal<RecordingState> threadLocalRecordingState = new ThreadLocal<>();
    private final CallIdGenerator initialCallIdGenerator;
    private final StartRecordingPolicy startRecordingPolicy;
    private final RecordDataWriter recordDataWriter;

    public Recorder(TypeResolver typeResolver, MethodRepository methodRepository, CallIdGenerator callIdGenerator, StartRecordingPolicy startRecordingPolicy, StorageWriter storageWriter) {
        this.typeResolver = typeResolver;
        this.methodRepository = methodRepository;
        this.recordDataWriter = new RecordDataWriter(storageWriter, methodRepository);
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
                threadLocalRecordingState.set(null);
            }
        }
    }

    public long startOrContinueRecordingOnMethodEnter(int methodId, @Nullable Object callee, Object[] args) {
        if (startRecordingPolicy.canStartRecording()) {
            RecordingState recordingState = threadLocalRecordingState.get();
            if (recordingState == null) {
                recordingState = new RecordingState();
                recordingState.setEnabled(false);
                threadLocalRecordingState.set(recordingState);

                CallRecordBuffer newCallRecordBuffer = new CallRecordBuffer(typeResolver, initialCallIdGenerator.getNextStartValue());
                recordingState.setCallRecordBuffer(newCallRecordBuffer);

                currentRecordingSessionCount.incrementAndGet();
                if (LoggingSettings.INFO_ENABLED) {
                    log.info("Started recording {} at method {}", newCallRecordBuffer.getRecordingMetadata().getId(), methodRepository.get(methodId).toShortString());
                }
                recordingState.setEnabled(true);
            }

            return onMethodEnter(recordingState, methodId, callee, args);
        } else {
            return -1;
        }
    }

    public long startOrContinueRecordingOnConstructorEnter(int methodId, Object[] args) {
        if (startRecordingPolicy.canStartRecording()) {
            RecordingState recordingState = threadLocalRecordingState.get();
            if (recordingState == null) {
                recordingState = new RecordingState();
                recordingState.setEnabled(false);
                threadLocalRecordingState.set(recordingState);

                CallRecordBuffer newCallRecordBuffer = new CallRecordBuffer(typeResolver, initialCallIdGenerator.getNextStartValue());
                recordingState.setCallRecordBuffer(newCallRecordBuffer);
                currentRecordingSessionCount.incrementAndGet();
                if (LoggingSettings.INFO_ENABLED) {
                    log.info("Started recording {} at method {}", newCallRecordBuffer.getRecordingMetadata().getId(), methodRepository.get(methodId).toShortString());
                }
                recordingState.setEnabled(true);
            }

            return onConstructorEnter(recordingState, methodId, args);
        } else {
            return -1;
        }
    }

    public long onConstructorEnter(int methodId, Object[] args) {
        return onMethodEnter(threadLocalRecordingState.get(), methodId, null, args);
    }

    public long onConstructorEnter(RecordingState recordingState, int methodId, Object[] args) {
        return onMethodEnter(recordingState, methodId, null, args);
    }

    public long onMethodEnter(int methodId, @Nullable Object callee, Object[] args) {
        return onMethodEnter(threadLocalRecordingState.get(), methodId, callee, args);
    }

    public long onMethodEnter(RecordingState recordingState, int methodId, @Nullable Object callee, Object[] args) {
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
                return callRecordBuffer.onMethodEnter(methodRepository.get(methodId), callee, args);
            } finally {
                recordingState.setEnabled(true);
            }
        } catch (Throwable err) {
            log.error("Error happened when recording", err);
            return -1;
        }
    }

    public void onConstructorExit(int methodId, Object result, long callId) {
        onMethodExit(methodId, result, null, callId);
    }

    public void onMethodExit(int methodId, Object result, Throwable thrown, long callId) {
        try {
            RecordingState recordingState = threadLocalRecordingState.get();
            if (recordingState == null || !recordingState.isEnabled()) return;
            CallRecordBuffer callRecords = recordingState.getCallRecordBuffer();
            if (callRecords == null) return;

            try {
                recordingState.setEnabled(false);
                Method method = methodRepository.get(methodId);
                callRecords.onMethodExit(method, result, thrown, callId);

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
                        threadLocalRecordingState.set(null);
                        currentRecordingSessionCount.decrementAndGet();
                        if (LoggingSettings.INFO_ENABLED) {
                            log.info("Finished recording {} at method {}, recorded {} calls", callRecords.getRecordingMetadata().getId(), method.toShortString(), callRecords.getTotalRecordedEnterCalls());
                        }
                    }

                    recordDataWriter.write(typeResolver, callRecords);
                }
            } finally {
                recordingState.setEnabled(true);
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
