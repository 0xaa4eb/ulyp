package com.ulyp.agent.queue;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

import com.ulyp.agent.AgentDataWriter;
import com.ulyp.agent.queue.disruptor.RecordingQueueDisruptor;
import com.ulyp.agent.queue.events.*;
import com.ulyp.core.metrics.Metrics;
import com.ulyp.core.recorders.*;
import com.ulyp.core.bytes.BufferBytesOut;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.core.util.ConcurrentSimpleObjectPool;
import com.ulyp.core.util.SystemPropertyUtil;
import org.agrona.concurrent.UnsafeBuffer;
import org.jetbrains.annotations.Nullable;

import com.lmax.disruptor.SleepingWaitStrategy;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.util.NamedThreadFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Main entry point for recorded call events. For most of the objects only their identity is recorded (identity hash code and type id).
 * Other objects like strings and numbers are immutable, so we can only pass a reference to object and
 * avoid serialization/recording. The state of object is then recorded to bytes in the background. For objects like collections
 * we must record in the caller thread and pass a buffer.
 * Currently, it has fixed capacity which should be addressed in near future
 */
@Slf4j
public class RecordingEventQueue implements AutoCloseable {

    private static final int RECORDING_QUEUE_SIZE = SystemPropertyUtil.getInt("ulyp.recording-queue.size", 16 * 1024);
    private static final int TMP_BUFFER_SIZE = SystemPropertyUtil.getInt("ulyp.recording-queue.tmp-buffer.size", 16 * 1024);
    private static final int TMP_BUFFER_ENTRIES = SystemPropertyUtil.getInt("ulyp.recording-queue.tmp-buffer.entries", 8);

    private final ThreadLocal<RecordingEventBatch> batchEventThreadLocal = ThreadLocal.withInitial(() -> {
        RecordingEventBatch eventBatch = new RecordingEventBatch();
        eventBatch.resetForUpcomingEvents();
        return eventBatch;
    });
    private final ConcurrentSimpleObjectPool<byte[]> bufferPool;
    private final TypeResolver typeResolver;
    private final RecordingQueueDisruptor disruptor;
    private final ScheduledExecutorService scheduledExecutorService;
    private final QueueBatchEventProcessorFactory eventProcessorFactory;

    public RecordingEventQueue(TypeResolver typeResolver, AgentDataWriter agentDataWriter, Metrics metrics) {
        this.typeResolver = typeResolver;
        this.bufferPool = new ConcurrentSimpleObjectPool<>(TMP_BUFFER_ENTRIES, () -> new byte[TMP_BUFFER_SIZE]);
        this.disruptor = new RecordingQueueDisruptor(
                RecordingEventBatch::new,
                RECORDING_QUEUE_SIZE,
                new QueueEventHandlerThreadFactory(),
                new SleepingWaitStrategy(3, TimeUnit.MILLISECONDS.toNanos(1)),
                metrics
        );
        this.eventProcessorFactory = new QueueBatchEventProcessorFactory(typeResolver, agentDataWriter);
        this.scheduledExecutorService = Executors.newScheduledThreadPool(
            1,
            NamedThreadFactory.builder().name("ulyp-recorder-queue-stats-reporter").daemon(true).build()
        );
        this.scheduledExecutorService.scheduleAtFixedRate(this::reportSeqDiff, 1, 1, TimeUnit.SECONDS);
    }

    public void start() {
        this.disruptor.handleEventsWith(eventProcessorFactory);
        this.disruptor.start();
    }

    public void enqueueRecordingStarted(RecordingMetadata recordingMetadata) {
        appendEvent(recordingMetadata.getId(), new RecordingStartedEvent(recordingMetadata));
    }

    public void enqueueRecordingFinished(int recordingId, long recordingFinishedTimeMillis) {
        appendEvent(recordingId, new RecordingFinishedEvent(recordingFinishedTimeMillis));
    }

    public void enqueueMethodEnter(int recordingId, int callId, int methodId, @Nullable Object callee, Object[] args) {
        int calleeTypeId;
        int calleeIdentityHashCode;
        if (callee != null) {
            calleeTypeId = typeResolver.get(callee).getId();
            calleeIdentityHashCode = System.identityHashCode(callee);
        } else {
            calleeTypeId = -1;
            calleeIdentityHashCode = 0;
        }

        appendEvent(recordingId, new EnterMethodRecordingEvent(
                callId,
                methodId,
                calleeTypeId,
                calleeIdentityHashCode,
                convert(args)));
    }

    public void enqueueMethodEnter(int recordingId, int callId, int methodId, @Nullable Object callee, Object[] args, long nanoTime) {
        int calleeTypeId;
        int calleeIdentityHashCode;
        if (callee != null) {
            calleeTypeId = typeResolver.get(callee).getId();
            calleeIdentityHashCode = System.identityHashCode(callee);
        } else {
            calleeTypeId = -1;
            calleeIdentityHashCode = 0;
        }
        appendEvent(recordingId, new TimestampedEnterMethodRecordingEvent(
                callId,
                methodId,
                calleeTypeId,
                calleeIdentityHashCode,
                convert(args),
                nanoTime));
    }

    public void enqueueMethodExit(int recordingId, int callId, Object returnValue, boolean thrown) {
        appendEvent(recordingId, new ExitMethodRecordingEvent(callId, convert(returnValue), thrown));
    }

    public void enqueueMethodExit(int recordingId, int callId, Object returnValue, boolean thrown, long nanoTime) {
        appendEvent(recordingId, new TimestampedExitMethodRecordingEvent(callId, convert(returnValue), thrown, nanoTime));
    }

    public void flush(int recordingId) {
        RecordingEventBatch eventBatch = batchEventThreadLocal.get();
        if (!eventBatch.isEmpty()) {
            eventBatch.setRecordingId(recordingId);
            disruptor.publish(eventBatch);
            eventBatch.resetForUpcomingEvents();
        }
    }

    private void appendEvent(int recordingId, RecordingEvent event) {
        RecordingEventBatch eventBatch = batchEventThreadLocal.get();
        eventBatch.add(event);
        if (eventBatch.isFull()) {
            eventBatch.setRecordingId(recordingId);
            disruptor.publish(eventBatch);
            eventBatch.resetForUpcomingEvents();
        }
    }

    private Object[] convert(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = convert(args[i]);
        }
        return args;
    }

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
            try (ConcurrentSimpleObjectPool.ObjectPoolClaim<byte[]> buffer = bufferPool.claim()) {
                BufferBytesOut output = new BufferBytesOut(new UnsafeBuffer(buffer.get()));
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
    }

    public void sync(Duration duration) throws InterruptedException, TimeoutException {
        long lastPublishedSeq = disruptor.getCursor();
        QueueBatchEventProcessor eventProcessor = eventProcessorFactory.getEventProcessor();
        long deadlineWaitTimeMs = System.currentTimeMillis() + duration.toMillis();
        while (System.currentTimeMillis() < deadlineWaitTimeMs) {
            if (eventProcessor.getSequence().get() >= lastPublishedSeq) {
                return;
            }
            LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(10));
        }
        throw new TimeoutException("Timed out waiting cakk record queue flush. " +
            "Waiting for seq " + lastPublishedSeq + " to be processed. Event handler prcoessed "
            + eventProcessor.getSequence().get());
    }

    private void reportSeqDiff() {
        QueueBatchEventProcessor eventProcessor = eventProcessorFactory.getEventProcessor();
        if (log.isDebugEnabled()) {
            log.debug("Seq difference: " + (disruptor.getCursor() - eventProcessor.getSequence().get()) +
                    ", event processor seq: " + eventProcessor.getSequence().get() +
                    ", published seq: " + disruptor.getCursor());
        }
    }

    @Override
    public void close() {
        scheduledExecutorService.shutdownNow();
        disruptor.halt();
    }
}