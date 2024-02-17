package com.ulyp.agent.queue;

import com.lmax.disruptor.EventProcessor;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.ulyp.agent.AgentDataWriter;
import com.ulyp.agent.queue.disruptor.EventProcessorFactory;
import com.ulyp.agent.queue.disruptor.RingBuffer;
import com.ulyp.agent.queue.events.EnterRecordQueueEvent;
import com.ulyp.agent.queue.events.ExitRecordQueueEvent;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.pool.ObjectPool;

import lombok.Getter;

public class QueueBatchEventProcessorFactory implements EventProcessorFactory<EventHolder> {

    private final TypeResolver typeResolver;
    private final AgentDataWriter agentDataWriter;
    private final ObjectPool<EnterRecordQueueEvent> enterRecordObjectPool;
    private final ObjectPool<ExitRecordQueueEvent> exitRecordObjectPool;
    @Getter
    private volatile QueueBatchEventProcessor eventProcessor;

    public QueueBatchEventProcessorFactory(
            TypeResolver typeResolver,
            AgentDataWriter agentDataWriter,
            ObjectPool<EnterRecordQueueEvent> enterRecordObjectPool,
            ObjectPool<ExitRecordQueueEvent> exitRecordObjectPool) {
        this.typeResolver = typeResolver;
        this.agentDataWriter = agentDataWriter;
        this.enterRecordObjectPool = enterRecordObjectPool;
        this.exitRecordObjectPool = exitRecordObjectPool;
    }

    @Override
    public EventProcessor createEventProcessor(RingBuffer<EventHolder> ringBuffer, Sequence[] barrierSequences) {
        if (eventProcessor != null) {
            return eventProcessor;
        }
        SequenceBarrier sequenceBarrier = ringBuffer.newBarrier(barrierSequences);
        return eventProcessor = new QueueBatchEventProcessor(
                ringBuffer,
                sequenceBarrier,
                typeResolver,
                agentDataWriter,
                enterRecordObjectPool,
                exitRecordObjectPool
        );
    }
}
