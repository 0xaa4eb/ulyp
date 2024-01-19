package com.ulyp.agent.queue;

import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.dsl.EventProcessorFactory;
import com.ulyp.agent.AgentDataWriter;
import com.ulyp.core.TypeResolver;

public class QueueBatchEventProcessorFactory implements EventProcessorFactory<EventHolder> {

    private final TypeResolver typeResolver;
    private final AgentDataWriter agentDataWriter;
    private volatile QueueBatchEventProcessor eventProcessor;

    public QueueBatchEventProcessorFactory(TypeResolver typeResolver, AgentDataWriter agentDataWriter) {
        this.typeResolver = typeResolver;
        this.agentDataWriter = agentDataWriter;
    }

    @Override
    public EventProcessor createEventProcessor(RingBuffer<EventHolder> ringBuffer, Sequence[] barrierSequences) {
        if (eventProcessor != null) {
            return eventProcessor;
        }
        SequenceBarrier sequenceBarrier = ringBuffer.newBarrier(barrierSequences);
        return eventProcessor = new QueueBatchEventProcessor(ringBuffer, sequenceBarrier, typeResolver, agentDataWriter);
    }

    public QueueBatchEventProcessor getEventProcessor() {
        return eventProcessor;
    }
}
