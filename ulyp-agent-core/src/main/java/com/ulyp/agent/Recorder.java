package com.ulyp.agent;

import com.ulyp.agent.policy.StartRecordingPolicy;
import com.ulyp.agent.queue.RecordingQueue;
import com.ulyp.core.*;
import com.ulyp.core.metrics.Counter;
import com.ulyp.core.metrics.Metrics;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.core.util.Preconditions;
import com.ulyp.core.util.SystemPropertyUtil;
import com.ulyp.storage.writer.RecordingDataWriter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

@SuppressWarnings("unused")
@Slf4j
@ThreadSafe
public class Recorder {

    private static final int RECORD_BUFFER_FLUSH_BYTE_SIZE = SystemPropertyUtil.getInt("ulyp.record.buffer-flush-byte-size", 16 * 1024 * 1024);
    private static final int RECORD_BUFFER_FLUSH_LIFETIME_MS = SystemPropertyUtil.getInt("ulyp.record.buffer-flush-lifetime-milli", 50);

    public static final AtomicInteger recordingIdGenerator = new AtomicInteger(-1);

    /**
    * Keeps current active recording session count. Based on the fact that most of the time there is no
    * recording sessions and this counter is equal to 0, it's possible to make a small performance optimization.
    * Advice code (see com.ulyp.agent.MethodCallRecordingAdvice) can first check if there are any recording sessions are active at all.
    * If there are any, then advice code will check thread local and know if there is recording session in this thread precisely.
    * This helps minimizing unneeded thread local lookups in the advice code
    */
    public static final AtomicInteger currentRecordingSessionCount = new AtomicInteger();

    private final MethodRepository methodRepository;
    private final ThreadLocal<RecordingState> threadLocalRecordingState = new ThreadLocal<>();
    private final StartRecordingPolicy startRecordingPolicy;
    @Getter
    private final RecordingQueue recordingQueue;
    private final Counter recordingsCounter;

    public Recorder(
        TypeResolver typeResolver,
        MethodRepository methodRepository,
        StartRecordingPolicy startRecordingPolicy,
        RecordingQueue recordingQueue,
        Metrics metrics) {
        this.methodRepository = methodRepository;
        this.recordingQueue = recordingQueue;
        this.startRecordingPolicy = startRecordingPolicy;
        this.recordingsCounter = metrics.getOrCreateCounter("recorder.count");
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
            if (recordingState.getRecordingId() >= 0) {
                recordingState.setEnabled(true);
            } else {
                threadLocalRecordingState.set(null);
            }
        }
    }

    public int startOrContinueRecordingOnMethodEnter(int methodId, @Nullable Object callee, Object[] args) {
        if (startRecordingPolicy.canStartRecording()) {
            RecordingState recordingState = threadLocalRecordingState.get();
            if (recordingState == null) {
                recordingState = new RecordingState();
                RecordingMetadata recordingMetadata = generateRecordingMetadata();
                recordingState.setRecordingMetadata(recordingMetadata);
                recordingState.setEnabled(false);

                threadLocalRecordingState.set(recordingState);

                currentRecordingSessionCount.incrementAndGet();
                if (LoggingSettings.DEBUG_ENABLED) {
                    log.debug("Started recording {} at method {}", recordingMetadata.getId(), methodRepository.get(methodId));
                }
                recordingsCounter.inc();
                recordingState.setEnabled(true);
                recordingQueue.enqueueRecordingMetadataUpdate(recordingMetadata);
            }

            return onMethodEnter(recordingState, methodId, callee, args);
        } else {
            return -1;
        }
    }

    public int startOrContinueRecordingOnConstructorEnter(int methodId, Object[] args) {
        if (startRecordingPolicy.canStartRecording()) {
            RecordingState recordingState = threadLocalRecordingState.get();
            if (recordingState == null) {
                recordingState = new RecordingState();
                recordingState.setEnabled(false);
                RecordingMetadata recordingMetadata = generateRecordingMetadata();
                recordingState.setRecordingMetadata(recordingMetadata);
                threadLocalRecordingState.set(recordingState);

                currentRecordingSessionCount.incrementAndGet();
                if (LoggingSettings.DEBUG_ENABLED) {
                    log.debug("Started recording {} at method {}", recordingMetadata.getId(), methodRepository.get(methodId).toShortString());
                }
                recordingsCounter.inc();
                recordingState.setEnabled(true);
                recordingQueue.enqueueRecordingMetadataUpdate(recordingMetadata);
            }

            return onConstructorEnter(recordingState, methodId, args);
        } else {
            return -1;
        }
    }

    public int onConstructorEnter(int methodId, Object[] args) {
        return onMethodEnter(threadLocalRecordingState.get(), methodId, null, args);
    }

    public int onConstructorEnter(RecordingState recordingState, int methodId, Object[] args) {
        return onMethodEnter(recordingState, methodId, null, args);
    }

    public int onMethodEnter(int methodId, @Nullable Object callee, Object[] args) {
        return onMethodEnter(threadLocalRecordingState.get(), methodId, callee, args);
    }

    public int onMethodEnter(RecordingState recordingState, int methodId, @Nullable Object callee, Object[] args) {
        try {
            if (recordingState == null || !recordingState.isEnabled()) {
                return -1;
            }

            try {
                recordingState.setEnabled(false);
                long nanoTime = 0;
                if (Settings.TIMESTAMPS_ENABLED) {
                    nanoTime = System.nanoTime();
                }
                int callId = recordingState.nextCallId();
                recordingQueue.enqueueMethodEnter(recordingState.getRecordingId(), callId, methodId, callee, args, nanoTime);
                return callId;
            } finally {
                recordingState.setEnabled(true);
            }
        } catch (Throwable err) {
            log.error("Error happened when recording", err);
            return -1;
        }
    }

    public void onConstructorExit(int methodId, Object result, int callId) {
        onMethodExit(methodId, result, null, callId);
    }

    public void onMethodExit(int methodId, Object result, Throwable thrown, int callId) {
        try {
            RecordingState recordingState = threadLocalRecordingState.get();
            if (recordingState == null || !recordingState.isEnabled()) return;

            try {
                recordingState.setEnabled(false);
                long nanoTime = 0;
                if (Settings.TIMESTAMPS_ENABLED) {
                    nanoTime = System.nanoTime();
                }
                if (callId == RecordingState.ROOT_CALL_RECORDING_ID) {
                    recordingQueue.enqueueRecordingMetadataUpdate(
                        recordingState.getRecordingMetadata().withCompleteTime(System.currentTimeMillis())
                    );
                }

                recordingQueue.enqueueMethodExit(recordingState.getRecordingId(), callId, thrown != null ? thrown : result, thrown != null, nanoTime);

                if (callId == RecordingState.ROOT_CALL_RECORDING_ID) {
                    int recordingId = recordingState.getRecordingId();
                    threadLocalRecordingState.set(null);
                    currentRecordingSessionCount.decrementAndGet();
                    if (LoggingSettings.DEBUG_ENABLED) {
                        Method method = methodRepository.get(methodId);
                        log.debug("Finished recording {} at method {}, recorded {} calls",
                            recordingState.getRecordingMetadata(),
                            method.toShortString(),
                            recordingState.getCallId()
                        );
                    }
                }
            } finally {
                recordingState.setEnabled(true);
            }
        } catch (Throwable err) {
            log.error("Error happened when recording", err);
        }
    }

    private RecordingMetadata generateRecordingMetadata() {
        List<String> stackTraceElements = Stream.of(new Exception().getStackTrace())
            .skip(2)
            .map(StackTraceElement::toString)
            .collect(Collectors.toList());

        return RecordingMetadata.builder()
            .id(recordingIdGenerator.incrementAndGet())
            .recordingStartedEpochMillis(System.currentTimeMillis())
            .logCreatedEpochMillis(System.currentTimeMillis())
            .threadId(Thread.currentThread().getId())
            .threadName(Thread.currentThread().getName())
            .stackTraceElements(stackTraceElements)
            .build();
    }

    @TestOnly
    RecordingState getRecordingState() {
        return threadLocalRecordingState.get();
    }
}
