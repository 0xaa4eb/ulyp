package com.ulyp.agent;

import com.ulyp.agent.policy.StartRecordingPolicy;
import com.ulyp.core.*;
import com.ulyp.core.metrics.Counter;
import com.ulyp.core.metrics.Metrics;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.core.util.Preconditions;
import com.ulyp.core.util.SystemPropertyUtil;
import com.ulyp.storage.writer.RecordingDataWriter;

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

    public static final AtomicInteger idGenerator = new AtomicInteger(-1);

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
    private final StartRecordingPolicy startRecordingPolicy;
    private final AgentDataWriter agentDataWriter;
    private final Counter recordingsCounter;

    public Recorder(
            TypeResolver typeResolver,
            MethodRepository methodRepository,
            StartRecordingPolicy startRecordingPolicy,
            RecordingDataWriter recordingDataWriter,
            Metrics metrics) {
        this.typeResolver = typeResolver;
        this.methodRepository = methodRepository;
        this.agentDataWriter = new AgentDataWriter(recordingDataWriter, methodRepository);
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
            if (recordingState.getCallRecordBuffer() != null) {
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
                recordingState.setRecordingId(recordingMetadata.getId());
                recordingState.setRecordingMetadata(recordingMetadata);
                recordingState.setEnabled(false);

                threadLocalRecordingState.set(recordingState);

                CallRecordBuffer newCallRecordBuffer = new CallRecordBuffer(recordingMetadata.getId());
                recordingState.setCallRecordBuffer(newCallRecordBuffer);

                currentRecordingSessionCount.incrementAndGet();
                if (LoggingSettings.DEBUG_ENABLED) {
                    log.debug("Started recording {} at method {}", recordingMetadata.getId(), methodRepository.get(methodId).toShortString());
                }
                recordingsCounter.inc();
                recordingState.setEnabled(true);
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

                CallRecordBuffer newCallRecordBuffer = new CallRecordBuffer(recordingMetadata.getId());
                recordingState.setCallRecordBuffer(newCallRecordBuffer);
                currentRecordingSessionCount.incrementAndGet();
                if (LoggingSettings.DEBUG_ENABLED) {
                    log.debug("Started recording {} at method {}", recordingMetadata.getId(), methodRepository.get(methodId).toShortString());
                }
                recordingsCounter.inc();
                recordingState.setEnabled(true);
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
            CallRecordBuffer callRecordBuffer = recordingState.getCallRecordBuffer();
            if (callRecordBuffer == null) {
                return -1;
            }

            try {
                recordingState.setEnabled(false);
                long nanoTime = 0;
                if (Settings.TIMESTAMPS_ENABLED) {
                    nanoTime = System.nanoTime();
                }
                return callRecordBuffer.recordMethodEnter(typeResolver, methodRepository.get(methodId), callee, args, nanoTime);
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
            CallRecordBuffer callRecordBuf = recordingState.getCallRecordBuffer();
            if (callRecordBuf == null) return;

            try {
                recordingState.setEnabled(false);
                long nanoTime = 0;
                if (Settings.TIMESTAMPS_ENABLED) {
                    nanoTime = System.nanoTime();
                }
                callRecordBuf.recordMethodExit(typeResolver, result, thrown, callId, nanoTime);

                RecordingMetadata recordingMetadata = recordingState.getRecordingMetadata();
                Preconditions.checkNotNull(recordingMetadata, "Recording metadata must not be null if recording is active");
                if (callRecordBuf.isComplete()) {
                    recordingMetadata.setRecordingCompletedEpochMillis(System.currentTimeMillis());
                }

                if (callRecordBuf.isComplete() ||
                    callRecordBuf.estimateBytesSize() > RECORD_BUFFER_FLUSH_BYTE_SIZE ||
                    (
                        (System.currentTimeMillis() - recordingState.getRecordingMetadata().getLogCreatedEpochMillis()) > RECORD_BUFFER_FLUSH_LIFETIME_MS
                            &&
                            callRecordBuf.getRecordedCallsSize() > 0
                    )) {

                    CallRecordBuffer newBuffer = callRecordBuf.cloneWithoutData();

                    if (!callRecordBuf.isComplete()) {
                        recordingState.setCallRecordBuffer(newBuffer);
                        recordingState.setRecordingMetadata(recordingMetadata.withNewCreationTimestamp());
                    } else {
                        threadLocalRecordingState.set(null);
                        currentRecordingSessionCount.decrementAndGet();
                        if (LoggingSettings.DEBUG_ENABLED) {
                            Method method = methodRepository.get(methodId);
                            log.debug("Finished recording {} at method {}, recorded {} calls", recordingMetadata.getId(), method.toShortString(), callRecordBuf.getTotalRecordedEnterCalls());
                        }
                    }

                    agentDataWriter.write(typeResolver, recordingMetadata, callRecordBuf);
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
            .id(idGenerator.incrementAndGet())
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
