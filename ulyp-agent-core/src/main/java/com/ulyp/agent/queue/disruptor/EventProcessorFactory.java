package com.ulyp.agent.queue.disruptor;

import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.Sequence;

public interface EventProcessorFactory<T> {

    EventProcessor createEventProcessor(RingBuffer<T> ringBuffer, Sequence[] barrierSequences);
}
