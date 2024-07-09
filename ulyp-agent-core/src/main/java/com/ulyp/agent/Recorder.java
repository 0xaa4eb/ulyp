package com.ulyp.agent;

import com.ulyp.agent.policy.StartRecordingPolicy;
import com.ulyp.agent.queue.RecordingEventQueue;
import com.ulyp.agent.util.RecordingStateStore;
import com.ulyp.core.*;
import com.ulyp.core.metrics.Counter;
import com.ulyp.core.metrics.Metrics;
import com.ulyp.core.util.BitUtil;
import com.ulyp.core.util.LoggingSettings;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.concurrent.ThreadSafe;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    private final Settings settings;
    private final TypeResolver typeResolver;
    private final MethodRepository methodRepository;
    private final ThreadLocal<RecordingState> threadLocalRecordingState = new ThreadLocal<>();
    private final RecordingStateStore recordingStateStore = new RecordingStateStore();
    private final StartRecordingPolicy startRecordingPolicy;
    @Getter
    private final RecordingEventQueue recordingEventQueue;
    private final Counter recordingsCounter;

    public Recorder(
            Settings settings,
            TypeResolver typeResolver,
            MethodRepository methodRepository,
            StartRecordingPolicy startRecordingPolicy,
            RecordingEventQueue recordingEventQueue,
            Metrics metrics) {
        this.settings = settings;
        this.typeResolver = typeResolver;
        this.methodRepository = methodRepository;
        this.recordingEventQueue = recordingEventQueue;
        this.startRecordingPolicy = startRecordingPolicy;
        this.recordingsCounter = metrics.getOrCreateCounter("recorder.count");
    }

    public boolean recordingIsActiveInCurrentThread() {
        RecordingState recordingState = threadLocalRecordingState.get();
        return recordingState != null && recordingState.isEnabled();
    }

    public RecordingState getCurrentRecordingState() {
        RecordingState recordingState = threadLocalRecordingState.get();
        if (recordingState != null && recordingState.isEnabled()) {
            return recordingState;
        } else {
            return null;
        }
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
            if (recordingState.getRecordingId() > 0) {
                recordingState.setEnabled(true);
            } else {
                threadLocalRecordingState.set(null);
            }
        }
    }

    public long startRecordingOnMethodEnter(int methodId, @Nullable Object callee, Object[] args) {
        if (startRecordingPolicy.canStartRecording()) {
            RecordingState recordingState = initializeRecordingState(methodId);

            return onMethodEnter(recordingState, methodId, callee, args);
        } else {
            return -1;
        }
    }

    @NotNull
    private RecordingState initializeRecordingState(int methodId) {
        RecordingState recordingState = threadLocalRecordingState.get();
        if (recordingState == null) {
            recordingState = new RecordingState();
            recordingState.setEnabled(false);
            int recordingId = recordingStateStore.add(recordingState);
            RecordingMetadata recordingMetadata = generateRecordingMetadata(recordingId);
            recordingState.setRecordingMetadata(recordingMetadata);
            threadLocalRecordingState.set(recordingState);
            RecordingEventBuffer recordingEventBuffer = new RecordingEventBuffer(recordingMetadata.getId(), settings, typeResolver);
            recordingState.setEventBuffer(recordingEventBuffer);

            currentRecordingSessionCount.incrementAndGet();
            if (LoggingSettings.DEBUG_ENABLED) {
                log.debug("Started recording {} at method {}", recordingMetadata.getId(), methodRepository.get(methodId));
            }
            recordingsCounter.inc();
            recordingState.setEnabled(true);
            recordingEventBuffer.appendRecordingStartedEvent(recordingMetadata);
        }
        return recordingState;
    }

    /**
     * @return call token which should be passed back to method {@link Recorder#onMethodEnter} when the corresponding
     * method completes
     */
    public long onMethodEnter(RecordingState recordingState, int methodId, @Nullable Object callee, Object[] args) {
        try {
            if (recordingState == null || !recordingState.isEnabled()) {
                return -1;
            }

            try {
                recordingState.setEnabled(false);
                int callId = recordingState.nextCallId();
                RecordingEventBuffer eventBuffer = recordingState.getEventBuffer();
                if (Settings.TIMESTAMPS_ENABLED) {
                    eventBuffer.appendMethodEnterEvent(callId, methodId, callee, args, System.nanoTime());
                } else {
                    eventBuffer.appendMethodEnterEvent(callId, methodId, callee, args);
                }
                if (eventBuffer.isFull()) {
                    recordingEventQueue.enqueue(eventBuffer);
                    eventBuffer.reset();
                }
                return BitUtil.longFromInts(recordingState.getRecordingId(), callId);
            } finally {
                recordingState.setEnabled(true);
            }
        } catch (Throwable err) {
            log.error("Error happened when recording", err);
            return -1;
        }
    }

    public void onMethodExit(int methodId, Object result, Throwable thrown, long callToken) {
        try {
            int recordingId = recordingId(callToken);
            int callId = callId(callToken);
            RecordingState recordingState = recordingStateStore.get(recordingId);
            if (recordingState == null || !recordingState.isEnabled()) return;

            try {
                recordingState.setEnabled(false);

                RecordingEventBuffer eventBuffer = recordingState.getEventBuffer();
                if (Settings.TIMESTAMPS_ENABLED) {
                    eventBuffer.appendMethodExitEvent(callId, thrown != null ? thrown : result, thrown != null, System.nanoTime());
                } else {
                    eventBuffer.appendMethodExitEvent(callId, thrown != null ? thrown : result, thrown != null);
                }

                if (callId == RecordingState.ROOT_CALL_RECORDING_ID) {
                    eventBuffer.appendRecordingFinishedEvent(System.currentTimeMillis());
                    recordingEventQueue.enqueue(eventBuffer);
                    recordingStateStore.remove(recordingId);
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
                } else {
                    if (eventBuffer.isFull()) {
                        recordingEventQueue.enqueue(eventBuffer);
                        eventBuffer.reset();
                    }
                }
            } finally {
                recordingState.setEnabled(true);
            }
        } catch (Throwable err) {
            log.error("Error happened when recording", err);
        }
    }

    private int recordingId(long callToken) {
        return (int) (callToken >> 32);
    }

    private int callId(long callToken) {
        return (int) callToken;
    }

    private RecordingMetadata generateRecordingMetadata(int recordingId) {
        List<String> stackTraceElements = Stream.of(new Exception().getStackTrace())
            .skip(2)
            .map(StackTraceElement::toString)
            .collect(Collectors.toList());

        return RecordingMetadata.builder()
            .id(recordingId)
            .recordingStartedMillis(System.currentTimeMillis())
            .logCreatedEpochMillis(System.currentTimeMillis())
            .threadId(Thread.currentThread().getId())
            .threadName(Thread.currentThread().getName())
            .stackTraceElements(stackTraceElements)
            .build();
    }

    /**
     * @return call token which should be passed back to method {@link Recorder#onMethodEnter} when the corresponding
     * method completes
     */
    @TestOnly
    public long onMethodEnter(int methodId, @Nullable Object callee, Object[] args) {
        return onMethodEnter(threadLocalRecordingState.get(), methodId, callee, args);
    }

    @TestOnly
    RecordingState getRecordingState() {
        return threadLocalRecordingState.get();
    }
}
