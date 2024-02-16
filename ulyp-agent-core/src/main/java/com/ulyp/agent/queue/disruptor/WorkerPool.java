package com.ulyp.agent.queue.disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.util.Util;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public final class WorkerPool<T> {
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Sequence workSequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
    private final RingBuffer<T> ringBuffer;
    private final WorkProcessor<?>[] workProcessors;

    @SafeVarargs
    public WorkerPool(
            final RingBuffer<T> ringBuffer,
            final SequenceBarrier sequenceBarrier,
            final ExceptionHandler<? super T> exceptionHandler,
            final WorkHandler<? super T>... workHandlers) {
        this.ringBuffer = ringBuffer;
        final int numWorkers = workHandlers.length;
        workProcessors = new WorkProcessor[numWorkers];

        for (int i = 0; i < numWorkers; i++) {
            workProcessors[i] = new WorkProcessor<>(
                    ringBuffer,
                    sequenceBarrier,
                    workHandlers[i],
                    exceptionHandler,
                    workSequence);
        }
    }

    public Sequence[] getWorkerSequences() {
        final Sequence[] sequences = new Sequence[workProcessors.length + 1];
        for (int i = 0, size = workProcessors.length; i < size; i++) {
            sequences[i] = workProcessors[i].getSequence();
        }
        sequences[sequences.length - 1] = workSequence;

        return sequences;
    }

    public RingBuffer<T> start(final Executor executor) {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("WorkerPool has already been started and cannot be restarted until halted.");
        }

        final long cursor = ringBuffer.getCursor();
        workSequence.set(cursor);

        for (WorkProcessor<?> processor : workProcessors) {
            processor.getSequence().set(cursor);
            executor.execute(processor);
        }

        return ringBuffer;
    }

    public void drainAndHalt() {
        Sequence[] workerSequences = getWorkerSequences();
        while (ringBuffer.getCursor() > Util.getMinimumSequence(workerSequences)) {
            Thread.yield();
        }

        for (WorkProcessor<?> processor : workProcessors) {
            processor.halt();
        }

        started.set(false);
    }

    public void halt() {
        for (WorkProcessor<?> processor : workProcessors) {
            processor.halt();
        }

        started.set(false);
    }

    public boolean isRunning() {
        return started.get();
    }
}
