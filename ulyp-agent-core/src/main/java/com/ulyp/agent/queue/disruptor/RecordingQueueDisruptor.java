package com.ulyp.agent.queue.disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.BasicExecutor;
import com.lmax.disruptor.dsl.ExceptionHandlerWrapper;
import com.lmax.disruptor.util.Util;
import com.ulyp.agent.RecordingEventBuffer;
import com.ulyp.agent.queue.RecordingEventDisruptorEntry;
import com.ulyp.core.metrics.Metrics;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Forked from the disruptor library. Main purpose is to gather some metrics from the parts the library doesn't expose and
 * provide additional methods
 */
public class RecordingQueueDisruptor {
    private final RingBuffer<RecordingEventDisruptorEntry> ringBuffer;
    private final Executor executor;
    private final ConsumerRepository<RecordingEventDisruptorEntry> consumerRepository = new ConsumerRepository<>();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final ExceptionHandler<RecordingEventDisruptorEntry> exceptionHandler = new ExceptionHandlerWrapper<>();

    public RecordingQueueDisruptor(
            final EventFactory<RecordingEventDisruptorEntry> eventFactory,
            final int ringBufferSize,
            final ThreadFactory threadFactory,
            final WaitStrategy waitStrategy,
            Metrics metrics) {
        this(RingBuffer.create(eventFactory, ringBufferSize, waitStrategy, metrics), new BasicExecutor(threadFactory));
    }

    private RecordingQueueDisruptor(final RingBuffer<RecordingEventDisruptorEntry> ringBuffer, final Executor executor) {
        this.ringBuffer = ringBuffer;
        this.executor = executor;
    }

    @SafeVarargs
    public final EventHandlerGroup handleEventsWith(final EventProcessorFactory<RecordingEventDisruptorEntry>... eventProcessorFactories) {
        final Sequence[] barrierSequences = new Sequence[0];
        return createEventProcessors(barrierSequences, eventProcessorFactories);
    }

    public EventHandlerGroup handleEventsWith(final EventProcessor... processors) {
        for (final EventProcessor processor : processors) {
            consumerRepository.add(processor);
        }

        final Sequence[] sequences = new Sequence[processors.length];
        for (int i = 0; i < processors.length; i++) {
            sequences[i] = processors[i].getSequence();
        }

        ringBuffer.addGatingSequences(sequences);

        return new EventHandlerGroup(this, consumerRepository, Util.getSequencesFor(processors));
    }

    public void publish(RecordingEventBuffer eventBuffer) {
        long next = ringBuffer.next(1);
        try {
            RecordingEventDisruptorEntry ringEntry = get(next);
            ringEntry.moveFrom(eventBuffer);
        } finally {
            ringBuffer.publish(next);
        }
    }

    public RingBuffer<RecordingEventDisruptorEntry> start() {
        checkOnlyStartedOnce();
        for (final ConsumerInfo consumerInfo : consumerRepository) {
            consumerInfo.start(executor);
        }

        return ringBuffer;
    }

    public void halt() {
        for (final ConsumerInfo consumerInfo : consumerRepository) {
            consumerInfo.halt();
        }
    }

    public void shutdown() {
        try {
            shutdown(-1, TimeUnit.MILLISECONDS);
        } catch (final TimeoutException e) {
            exceptionHandler.handleOnShutdownException(e);
        }
    }

    public void shutdown(final long timeout, final TimeUnit timeUnit) throws TimeoutException {
        final long timeOutAt = System.currentTimeMillis() + timeUnit.toMillis(timeout);
        while (hasBacklog()) {
            if (timeout >= 0 && System.currentTimeMillis() > timeOutAt) {
                throw TimeoutException.INSTANCE;
            }
            // Busy spin
        }
        halt();
    }

    public long getCursor() {
        return ringBuffer.getCursor();
    }

    public RecordingEventDisruptorEntry get(final long sequence) {
        return ringBuffer.get(sequence);
    }

    private boolean hasBacklog() {
        final long cursor = ringBuffer.getCursor();
        for (final Sequence consumer : consumerRepository.getLastSequenceInChain(false)) {
            if (cursor > consumer.get()) {
                return true;
            }
        }
        return false;
    }

    EventHandlerGroup createEventProcessors(
            final Sequence[] barrierSequences,
            final EventHandler<RecordingEventDisruptorEntry>[] eventHandlers) {
        checkNotStarted();

        final Sequence[] processorSequences = new Sequence[eventHandlers.length];
        final SequenceBarrier barrier = ringBuffer.newBarrier(barrierSequences);

        for (int i = 0, eventHandlersLength = eventHandlers.length; i < eventHandlersLength; i++) {
            final EventHandler<RecordingEventDisruptorEntry> eventHandler = eventHandlers[i];

            final BatchEventProcessor<RecordingEventDisruptorEntry> batchEventProcessor =
                    new BatchEventProcessor<>(ringBuffer, barrier, eventHandler);

            batchEventProcessor.setExceptionHandler(exceptionHandler);
            consumerRepository.add(batchEventProcessor, eventHandler, barrier);
            processorSequences[i] = batchEventProcessor.getSequence();
        }

        updateGatingSequencesForNextInChain(barrierSequences, processorSequences);

        return new EventHandlerGroup(this, consumerRepository, processorSequences);
    }

    private void updateGatingSequencesForNextInChain(final Sequence[] barrierSequences, final Sequence[] processorSequences) {
        if (processorSequences.length > 0) {
            ringBuffer.addGatingSequences(processorSequences);
            for (final Sequence barrierSequence : barrierSequences) {
                ringBuffer.removeGatingSequence(barrierSequence);
            }
            consumerRepository.unMarkEventProcessorsAsEndOfChain(barrierSequences);
        }
    }

    EventHandlerGroup createEventProcessors(
            final Sequence[] barrierSequences, final EventProcessorFactory<RecordingEventDisruptorEntry>[] processorFactories) {
        final EventProcessor[] eventProcessors = new EventProcessor[processorFactories.length];
        for (int i = 0; i < processorFactories.length; i++) {
            eventProcessors[i] = processorFactories[i].createEventProcessor(ringBuffer, barrierSequences);
        }

        return handleEventsWith(eventProcessors);
    }

    EventHandlerGroup createWorkerPool(
            final Sequence[] barrierSequences, final WorkHandler<RecordingEventDisruptorEntry>[] workHandlers) {
        final SequenceBarrier sequenceBarrier = ringBuffer.newBarrier(barrierSequences);
        final WorkerPool<RecordingEventDisruptorEntry> workerPool = new WorkerPool<>(ringBuffer, sequenceBarrier, exceptionHandler, workHandlers);


        consumerRepository.add(workerPool, sequenceBarrier);

        final Sequence[] workerSequences = workerPool.getWorkerSequences();

        updateGatingSequencesForNextInChain(barrierSequences, workerSequences);

        return new EventHandlerGroup(this, consumerRepository, workerSequences);
    }

    private void checkNotStarted() {
        if (started.get()) {
            throw new IllegalStateException("All event handlers must be added before calling starts.");
        }
    }

    private void checkOnlyStartedOnce() {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("Disruptor.start() must only be called once.");
        }
    }

    @Override
    public String toString() {
        return "Disruptor{" +
                "ringBuffer=" + ringBuffer +
                ", started=" + started +
                ", executor=" + executor +
                '}';
    }
}
