package com.ulyp.agent.queue;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

import org.jetbrains.annotations.Nullable;

import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.ulyp.agent.RecordDataWriter;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.QueuedIdentityObject;
import com.ulyp.core.recorders.IdentityRecorder;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.RecorderChooser;
import com.ulyp.core.util.NamedThreadFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CallRecordQueue implements AutoCloseable {

    private final TypeResolver typeResolver;
    private final Disruptor<EventHolder> disruptor;
    private final ScheduledExecutorService scheduledExecutorService;
    private final QueueBatchEventProcessorFactory eventProcessorFactory;

    public CallRecordQueue(TypeResolver typeResolver, RecordDataWriter recordDataWriter) {
        this.typeResolver = typeResolver;
        this.disruptor = new Disruptor<>(
            EventHolder::new,
            1024 * 1024, // TODO configurable
            new QueueEventHandlerThreadFactory(),
            ProducerType.MULTI,
            new SleepingWaitStrategy()
        );
        this.eventProcessorFactory = new QueueBatchEventProcessorFactory(typeResolver, recordDataWriter);
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
        disruptor.publishEvent((entry, seq) -> entry.event = new RecordingMetadataQueueEvent(recordingMetadata));
    }

    public void enqueueMethodEnter(int recordingId, int callId, int methodId, @Nullable Object callee, Object[] args, long nanoTime) {
        disruptor.publishEvent((entry, seq) -> entry.event = new EnterRecordQueueEvent(recordingId, callId, methodId, convert(callee), convert(args), nanoTime));
    }

    public void enqueueMethodExit(int recordingId, int callId, Object returnValue, boolean thrown, long nanoTime) {
        disruptor.publishEvent((entry, seq) -> entry.event = new ExitRecordQueueEvent(recordingId, callId, convert(returnValue), thrown, nanoTime));
    }

    private Object[] convert(Object[] args) {
        Object[] converted = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            converted[i] = convert(args[i]);
        }
        return converted;
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
                return new QueuedIdentityObject(type, value);
            } else {
                return value;
            }
        } else {
            throw new RuntimeException("not supported yet");
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
        log.info("Seq difference: " + (disruptor.getCursor() - eventProcessor.getSequence().get()) +
            ", event processor seq: " + eventProcessor.getSequence().get() +
            ", published seq: " + disruptor.getCursor());
    }

    @Override
    public void close() {
        scheduledExecutorService.shutdownNow();
        disruptor.halt();
    }
}
