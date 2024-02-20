package com.ulyp.agent.queue;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

import com.ulyp.agent.AgentDataWriter;
import com.ulyp.agent.queue.disruptor.RecordingQueueDisruptor;
import com.ulyp.agent.queue.events.EnterRecordQueueEvent;
import com.ulyp.agent.queue.events.ExitRecordQueueEvent;
import com.ulyp.agent.queue.events.TimestampedEnterRecordQueueEvent;
import com.ulyp.agent.queue.events.TimestampedExitRecordQueueEvent;
import com.ulyp.core.metrics.Metrics;
import com.ulyp.core.recorders.*;
import com.ulyp.core.bytes.BufferBinaryOutput;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.core.util.SmallObjectPool;
import org.agrona.concurrent.UnsafeBuffer;
import org.jetbrains.annotations.Nullable;

import com.lmax.disruptor.SleepingWaitStrategy;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.util.NamedThreadFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RecordingQueue implements AutoCloseable {

    private final SmallObjectPool<byte[]> bufferPool;
    private final TypeResolver typeResolver;
    private final RecordingQueueDisruptor disruptor;
    private final ScheduledExecutorService scheduledExecutorService;
    private final QueueBatchEventProcessorFactory eventProcessorFactory;

    public RecordingQueue(TypeResolver typeResolver, AgentDataWriter agentDataWriter, Metrics metrics) {
        this.typeResolver = typeResolver;
        this.bufferPool = new SmallObjectPool<>(8, () -> new byte[16 * 1024]); // TODO configurable
        this.disruptor = new RecordingQueueDisruptor(
                EventHolder::new,
                1024 * 1024, // TODO configurable
                new QueueEventHandlerThreadFactory(),
                new SleepingWaitStrategy(),
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

    public void enqueueRecordingMetadataUpdate(RecordingMetadata recordingMetadata) {
        disruptor.publish(new RecordingMetadataQueueEvent(recordingMetadata));
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
        disruptor.publish(new EnterRecordQueueEvent(
                recordingId,
                callId,
                methodId,
                calleeTypeId,
                calleeIdentityHashCode,
                convert(args))
        );
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
        disruptor.publish(new TimestampedEnterRecordQueueEvent(
                recordingId,
                callId,
                methodId,
                calleeTypeId,
                calleeIdentityHashCode,
                convert(args),
                nanoTime)
        );
    }

    public void enqueueMethodExit(int recordingId, int callId, Object returnValue, boolean thrown) {
        disruptor.publish(new ExitRecordQueueEvent(recordingId, callId, convert(returnValue), thrown));
    }

    public void enqueueMethodExit(int recordingId, int callId, Object returnValue, boolean thrown, long nanoTime) {
        disruptor.publish(new TimestampedExitRecordQueueEvent(recordingId, callId, convert(returnValue), thrown, nanoTime));
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
            try (SmallObjectPool.ObjectPoolClaim<byte[]> buffer = bufferPool.claim()) {
                BufferBinaryOutput output = new BufferBinaryOutput(new UnsafeBuffer(buffer.get()));
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
