package com.ulyp.agent.queue.disruptor;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;

import java.util.concurrent.Executor;

class EventProcessorInfo<T> implements ConsumerInfo {
    private final EventProcessor eventprocessor;
    private final EventHandler<? super T> handler;
    private final SequenceBarrier barrier;
    private boolean endOfChain = true;

    EventProcessorInfo(final EventProcessor eventprocessor, final EventHandler<? super T> handler, final SequenceBarrier barrier) {
        this.eventprocessor = eventprocessor;
        this.handler = handler;
        this.barrier = barrier;
    }

    public EventProcessor getEventProcessor() {
        return eventprocessor;
    }

    @Override
    public Sequence[] getSequences() {
        return new Sequence[]{eventprocessor.getSequence()};
    }

    @Override
    public SequenceBarrier getBarrier() {
        return barrier;
    }

    @Override
    public boolean isEndOfChain() {
        return endOfChain;
    }

    @Override
    public void start(final Executor executor) {
        executor.execute(eventprocessor);
    }

    @Override
    public void halt() {
        eventprocessor.halt();
    }

    @Override
    public void markAsUsedInBarrier() {
        endOfChain = false;
    }

    @Override
    public boolean isRunning() {
        return eventprocessor.isRunning();
    }
}
