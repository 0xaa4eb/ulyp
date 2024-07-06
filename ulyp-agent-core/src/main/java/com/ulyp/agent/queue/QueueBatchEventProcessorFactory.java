package com.ulyp.agent.queue;

import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.ulyp.agent.AgentDataWriter;
import com.ulyp.agent.queue.disruptor.EventProcessorFactory;
import com.ulyp.agent.queue.disruptor.RingBuffer;
import com.ulyp.core.TypeResolver;
import lombok.Getter;

public class QueueBatchEventProcessorFactory implements EventProcessorFactory<RecordingEventBatch> {

    private final TypeResolver typeResolver;
    private final AgentDataWriter agentDataWriter;
    @Getter
    private volatile QueueBatchEventProcessor eventProcessor;

    public QueueBatchEventProcessorFactory(TypeResolver typeResolver, AgentDataWriter agentDataWriter) {
        this.typeResolver = typeResolver;
        this.agentDataWriter = agentDataWriter;
    }

    @Override
    public EventProcessor createEventProcessor(RingBuffer<RecordingEventBatch> ringBuffer, Sequence[] barrierSequences) {
        if (eventProcessor != null) {
            return eventProcessor;
        }
        SequenceBarrier sequenceBarrier = ringBuffer.newBarrier(barrierSequences);
        eventProcessor = new QueueBatchEventProcessor(ringBuffer, sequenceBarrier, typeResolver, agentDataWriter);
        return eventProcessor;
    }
}
