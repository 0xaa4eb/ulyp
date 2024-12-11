package com.ulyp.agent;

import com.ulyp.agent.options.AgentOptions;
import com.ulyp.agent.policy.StartRecordingPolicy;
import com.ulyp.agent.queue.RecordingEventQueue;
import com.ulyp.agent.util.RecordingContextStore;
import com.ulyp.core.Method;
import com.ulyp.core.MethodRepository;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.metrics.Counter;
import com.ulyp.core.metrics.Metrics;
import com.ulyp.core.util.BitUtil;
import com.ulyp.core.util.LoggingSettings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.concurrent.ThreadSafe;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final AgentOptions options;
    private final TypeResolver typeResolver;
    private final MethodRepository methodRepository;
    private final ThreadLocal<RecordingThreadLocalContext> threadLocalRecordingCtx = new ThreadLocal<>();
    private final RecordingContextStore recordingContextStore = new RecordingContextStore();
    private final StartRecordingPolicy startRecordingPolicy;
    @Getter
    private final RecordingEventQueue recordingEventQueue;
    private final Counter recordingsCounter;

    public Recorder(
            AgentOptions options,
            TypeResolver typeResolver,
            MethodRepository methodRepository,
            StartRecordingPolicy startRecordingPolicy,
            RecordingEventQueue recordingEventQueue,
            Metrics metrics) {
        this.options = options;
        this.typeResolver = typeResolver;
        this.methodRepository = methodRepository;
        this.recordingEventQueue = recordingEventQueue;
        this.startRecordingPolicy = startRecordingPolicy;
        this.recordingsCounter = metrics.getOrCreateCounter("recorder.count");
    }

    public RecordingThreadLocalContext getCtx() {
        RecordingThreadLocalContext recordingCtx = threadLocalRecordingCtx.get();
        if (recordingCtx != null && recordingCtx.isEnabled()) {
            return recordingCtx;
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
        RecordingThreadLocalContext recordingCtx = threadLocalRecordingCtx.get();
        if (recordingCtx != null) {
            recordingCtx.setEnabled(false);
        } else {
            recordingCtx = new RecordingThreadLocalContext(options, typeResolver);
            recordingCtx.setEnabled(false);
            threadLocalRecordingCtx.set(recordingCtx);
        }
    }

    public void enableRecording() {
        RecordingThreadLocalContext recordingCtx = threadLocalRecordingCtx.get();
        if (recordingCtx != null) {
            if (recordingCtx.getRecordingId() > 0) {
                recordingCtx.setEnabled(true);
            } else {
                threadLocalRecordingCtx.remove();
            }
        }
    }

    /**
     * Starts recording (if possible)
     * @return call token which should be passed back to method {@link Recorder#onMethodExit} when the corresponding
     * method completes
     */
    public long startRecordingOnMethodEnter(int methodId, @Nullable Object callee, Object[] args) {
        if (startRecordingPolicy.canStartRecording()) {
            RecordingThreadLocalContext recordingCtx = initializeRecordingCtx(methodId);

            return onMethodEnter(recordingCtx, methodId, callee, args);
        } else {
            return -1;
        }
    }

    /**
     * Starts recording (if possible) on constructor enter
     * @return call token which should be passed back to method {@link Recorder#onMethodExit} when the corresponding
     * method completes
     */
    public long startRecordingOnConstructorEnter(int methodId, Class<?> declaringClass, Object[] args) {
        if (startRecordingPolicy.canStartRecording()) {
            RecordingThreadLocalContext recordingCtx = initializeRecordingCtx(methodId);

            recordingCtx.getConstructingTypes().push(typeResolver.get(declaringClass).getId());

            return onMethodEnter(recordingCtx, methodId, null, args);
        } else {
            return -1;
        }
    }

    @NotNull
    private RecordingThreadLocalContext initializeRecordingCtx(int methodId) {
        RecordingThreadLocalContext recordingCtx = threadLocalRecordingCtx.get();
        if (recordingCtx == null) {
            recordingCtx = new RecordingThreadLocalContext(options, typeResolver);
            recordingCtx.setEnabled(false);
            int recordingId = recordingContextStore.add(recordingCtx);
            RecordingMetadata recordingMetadata = generateRecordingMetadata(recordingId);
            recordingCtx.setRecordingMetadata(recordingMetadata);
            threadLocalRecordingCtx.set(recordingCtx);
            RecordingEventBuffer recordingEventBuffer = new RecordingEventBuffer(recordingMetadata.getId());
            recordingCtx.setEventBuffer(recordingEventBuffer);

            currentRecordingSessionCount.incrementAndGet();
            if (LoggingSettings.DEBUG_ENABLED) {
                log.debug("Started recording {} at method {}", recordingMetadata.getId(), methodRepository.get(methodId));
            }
            recordingsCounter.inc();
            recordingCtx.setEnabled(true);
            recordingEventBuffer.appendRecordingStartedEvent(recordingMetadata);
        }
        return recordingCtx;
    }

    public long onConstructorEnter(RecordingThreadLocalContext ctx, int methodId, Class<?> declaringClass, Object[] args) {
        if (ctx == null || !ctx.isEnabled()) {
            return -1;
        }

        ctx.getConstructingTypes().push(typeResolver.get(declaringClass).getId());

        return onMethodEnter(ctx, methodId, null, args);
    }

    /**
     * Specialized version of recording method enter logic for methods which have any number of parameters.
     *
     * @return call token which should be passed back to method {@link Recorder#onMethodExit} when the corresponding
     * method completes
     */
    public long onMethodEnter(RecordingThreadLocalContext ctx, int methodId, @Nullable Object callee, Object[] args) {
        try {
            if (ctx == null || !ctx.isEnabled()) {
                return -1;
            }

            try {
                ctx.setEnabled(false);
                ObjectRecordingConverter objectConverter = ctx.getRecordingObjectConverter();
                int callId = ctx.nextCallId();
                RecordingEventBuffer eventBuffer = ctx.getEventBuffer();
                if (AgentOptions.TIMESTAMPS_ENABLED) {
                    eventBuffer.appendMethodEnterEvent(
                            methodId,
                            callee,
                            objectConverter.prepare(args, ctx.getConstructingTypes()),
                            System.nanoTime()
                    );
                } else {
                    eventBuffer.appendMethodEnterEvent(
                            methodId,
                            callee,
                            objectConverter.prepare(args, ctx.getConstructingTypes())
                    );
                }
                dropIfFull(eventBuffer);
                return BitUtil.longFromInts(ctx.getRecordingId(), callId);
            } finally {
                ctx.setEnabled(true);
            }
        } catch (Throwable err) {
            log.error("Error happened when recording", err);
            return -1;
        }
    }

    /**
     * Specialized version of recording method enter logic for methods which accept only one parameter.
     *
     * @return call token which should be passed back to method {@link Recorder#onMethodExit} when the corresponding
     * method completes
     */
    public long onMethodEnter(RecordingThreadLocalContext ctx, int methodId, @Nullable Object callee, Object arg) {
        try {
            if (ctx == null || !ctx.isEnabled()) {
                return -1;
            }

            try {
                ctx.setEnabled(false);
                ObjectRecordingConverter objectConverter = ctx.getRecordingObjectConverter();
                int callId = ctx.nextCallId();
                RecordingEventBuffer eventBuffer = ctx.getEventBuffer();
                if (AgentOptions.TIMESTAMPS_ENABLED) {
                    eventBuffer.appendMethodEnterEvent(
                            methodId,
                            callee,
                            objectConverter.prepare(arg, ctx.getConstructingTypes()),
                            System.nanoTime()
                    );
                } else {
                    eventBuffer.appendMethodEnterEvent(
                            methodId,
                            callee,
                            objectConverter.prepare(arg, ctx.getConstructingTypes())
                    );
                }
                dropIfFull(eventBuffer);
                return BitUtil.longFromInts(ctx.getRecordingId(), callId);
            } finally {
                ctx.setEnabled(true);
            }
        } catch (Throwable err) {
            log.error("Error happened when recording", err);
            return -1;
        }
    }

    /**
     * Specialized version of recording method enter logic for methods which accept only two parameters.
     *
     * @return call token which should be passed back to method {@link Recorder#onMethodExit} when the corresponding
     * method completes
     */
    public long onMethodEnter(RecordingThreadLocalContext ctx, int methodId, @Nullable Object callee, Object arg1, Object arg2) {
        try {
            if (ctx == null || !ctx.isEnabled()) {
                return -1;
            }

            try {
                ctx.setEnabled(false);
                ObjectRecordingConverter objectConverter = ctx.getRecordingObjectConverter();
                int callId = ctx.nextCallId();
                RecordingEventBuffer eventBuffer = ctx.getEventBuffer();
                if (AgentOptions.TIMESTAMPS_ENABLED) {
                    eventBuffer.appendMethodEnterEvent(
                            methodId,
                            callee,
                            objectConverter.prepare(arg1, ctx.getConstructingTypes()),
                            objectConverter.prepare(arg2, ctx.getConstructingTypes()),
                            System.nanoTime()
                    );
                } else {
                    eventBuffer.appendMethodEnterEvent(
                            methodId,
                            callee,
                            objectConverter.prepare(arg1, ctx.getConstructingTypes()),
                            objectConverter.prepare(arg2, ctx.getConstructingTypes())
                    );
                }
                dropIfFull(eventBuffer);
                return BitUtil.longFromInts(ctx.getRecordingId(), callId);
            } finally {
                ctx.setEnabled(true);
            }
        } catch (Throwable err) {
            log.error("Error happened when recording", err);
            return -1;
        }
    }

    /**
     * Specialized version of recording method enter logic for methods which accept only two parameters.
     *
     * @return call token which should be passed back to method {@link Recorder#onMethodExit} when the corresponding
     * method completes
     */
    public long onMethodEnter(RecordingThreadLocalContext ctx, int methodId, @Nullable Object callee, Object arg1, Object arg2, Object arg3) {
        try {
            if (ctx == null || !ctx.isEnabled()) {
                return -1;
            }

            try {
                ctx.setEnabled(false);
                ObjectRecordingConverter objectConverter = ctx.getRecordingObjectConverter();
                int callId = ctx.nextCallId();
                RecordingEventBuffer eventBuffer = ctx.getEventBuffer();
                if (AgentOptions.TIMESTAMPS_ENABLED) {
                    eventBuffer.appendMethodEnterEvent(
                            methodId,
                            callee,
                            objectConverter.prepare(arg1, ctx.getConstructingTypes()),
                            objectConverter.prepare(arg2, ctx.getConstructingTypes()),
                            objectConverter.prepare(arg3, ctx.getConstructingTypes()),
                            System.nanoTime()
                    );
                } else {
                    eventBuffer.appendMethodEnterEvent(
                            methodId,
                            callee,
                            objectConverter.prepare(arg1, ctx.getConstructingTypes()),
                            objectConverter.prepare(arg2, ctx.getConstructingTypes()),
                            objectConverter.prepare(arg3, ctx.getConstructingTypes())
                    );
                }
                dropIfFull(eventBuffer);
                return BitUtil.longFromInts(ctx.getRecordingId(), callId);
            } finally {
                ctx.setEnabled(true);
            }
        } catch (Throwable err) {
            log.error("Error happened when recording", err);
            return -1;
        }
    }

    /**
     * Specialized version of recording method enter logic for methods which accept no parameters.
     *
     * @return call token which should be passed back to method {@link Recorder#onMethodExit} when the corresponding
     * method completes
     */
    public long onMethodEnter(RecordingThreadLocalContext ctx, int methodId, @Nullable Object callee) {
        try {
            if (ctx == null || !ctx.isEnabled()) {
                return -1;
            }

            try {
                ctx.setEnabled(false);
                int callId = ctx.nextCallId();
                RecordingEventBuffer eventBuffer = ctx.getEventBuffer();
                if (AgentOptions.TIMESTAMPS_ENABLED) {
                    eventBuffer.appendMethodEnterEvent(methodId, callee, System.nanoTime());
                } else {
                    eventBuffer.appendMethodEnterEvent(methodId, callee);
                }
                dropIfFull(eventBuffer);
                return BitUtil.longFromInts(ctx.getRecordingId(), callId);
            } finally {
                ctx.setEnabled(true);
            }
        } catch (Throwable err) {
            log.error("Error happened when recording", err);
            return -1;
        }
    }

    public void onConstructorExit(int methodId, Object result, Class<?> declaringClass, Throwable thrown, long callToken) {
        int recordingId = recordingId(callToken);
        int callId = callId(callToken);
        RecordingThreadLocalContext ctx = recordingContextStore.get(recordingId);
        if (ctx == null || !ctx.isEnabled()) return;

        ctx.getConstructingTypes().pop(typeResolver.get(declaringClass).getId());

        onMethodExit(methodId, result, thrown, callToken);
    }

    /**
     * Records method exit.
     *
     * @param callToken call token which was obtained by {@link Recorder#onMethodEnter} call
     */
    public void onMethodExit(int methodId, Object result, Throwable thrown, long callToken) {
        try {
            int recordingId = recordingId(callToken);
            int callId = callId(callToken);
            RecordingThreadLocalContext ctx = recordingContextStore.get(recordingId);
            if (ctx == null || !ctx.isEnabled()) return;

            try {
                ctx.setEnabled(false);

                ObjectRecordingConverter objectConverter = ctx.getRecordingObjectConverter();
                RecordingEventBuffer eventBuffer = ctx.getEventBuffer();
                if (AgentOptions.TIMESTAMPS_ENABLED) {
                    eventBuffer.appendMethodExitEvent(
                            callId,
                            objectConverter.prepare(thrown != null ? thrown : result, ctx.getConstructingTypes()),
                            thrown != null,
                            System.nanoTime()
                    );
                } else {
                    eventBuffer.appendMethodExitEvent(
                            callId,
                            objectConverter.prepare(thrown != null ? thrown : result, ctx.getConstructingTypes()),
                            thrown != null
                    );
                }

                if (callId == RecordingThreadLocalContext.ROOT_CALL_RECORDING_ID) {
                    eventBuffer.appendRecordingFinishedEvent(System.currentTimeMillis());
                    recordingEventQueue.enqueue(eventBuffer);
                    recordingContextStore.remove(recordingId);
                    threadLocalRecordingCtx.remove();
                    currentRecordingSessionCount.decrementAndGet();
                    if (LoggingSettings.DEBUG_ENABLED) {
                        Method method = methodRepository.get(methodId);
                        log.debug("Finished recording {} at method {}, recorded {} calls",
                            ctx.getRecordingMetadata(),
                            method.toShortString(),
                            ctx.getCallId()
                        );
                    }
                } else {
                    dropIfFull(eventBuffer);
                }
            } finally {
                ctx.setEnabled(true);
            }
        } catch (Throwable err) {
            log.error("Error happened when recording", err);
        }
    }

    private void dropIfFull(RecordingEventBuffer eventBuffer) {
        if (eventBuffer.isFull()) {
            recordingEventQueue.enqueue(eventBuffer);
            eventBuffer.reset();
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
        return onMethodEnter(threadLocalRecordingCtx.get(), methodId, callee, args);
    }

    @TestOnly
    RecordingThreadLocalContext getRecordingCtx() {
        return threadLocalRecordingCtx.get();
    }
}
