package com.ulyp.agent.queue;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

import com.ulyp.agent.AgentDataWriter;
import com.ulyp.agent.RecordingEventBuffer;
import com.ulyp.agent.queue.disruptor.RecordingQueueDisruptor;
import com.ulyp.core.metrics.Metrics;
import com.ulyp.core.util.SystemPropertyUtil;

import com.lmax.disruptor.SleepingWaitStrategy;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.util.NamedThreadFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Main entry point for recorded call events. For most of the objects only their identity is recorded (identity hash code and type id).
 * Other objects like strings and numbers are immutable, so we can only pass a reference to object and
 * avoid serialization/recording. The state of object is then recorded to bytes in the background. For objects like collections
 * we must record in the caller thread and pass a buffer.
 */
@Slf4j
public class RecordingEventQueue implements AutoCloseable {

    private static final int RECORDING_QUEUE_SIZE = SystemPropertyUtil.getInt("ulyp.recording-queue.size", 64 * 1024);

    private final RecordingQueueDisruptor disruptor;
    private final ScheduledExecutorService scheduledExecutorService;
    private final QueueBatchEventProcessorFactory eventProcessorFactory;

    public RecordingEventQueue(TypeResolver typeResolver, AgentDataWriter agentDataWriter, Metrics metrics) {
        this.disruptor = new RecordingQueueDisruptor(
                RecordingEventDisruptorEntry::new,
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

    public void enqueue(RecordingEventBuffer eventBuffer) {
        disruptor.publish(eventBuffer);
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
