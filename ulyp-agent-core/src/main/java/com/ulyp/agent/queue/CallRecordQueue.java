package com.ulyp.agent.queue;

import org.jetbrains.annotations.Nullable;

import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.ulyp.agent.RecordDataWriter;

public class CallRecordQueue {

    private final Disruptor<CallRecordQueueEntry> disruptor;

    public CallRecordQueue(RecordDataWriter recordDataWriter) {
        this.disruptor = new Disruptor<>(
            CallRecordQueueEntry::new,
            256 * 1024,
            DaemonThreadFactory.INSTANCE,
            ProducerType.MULTI,
            new SleepingWaitStrategy()
        );
        disruptor.start();
    }

    public void enqueueMethodEnter(int callId, int methodId, @Nullable Object callee, Object[] args) {
        disruptor.publishEvent((entry, seq) -> entry.setEnterCall(callId, methodId, callee, args));
    }

    public void enqueueMethodExit(int callId, Object returnValue, boolean thrown) {
        disruptor.publishEvent((entry, seq) -> entry.setExitCall(callId, returnValue, thrown));
    }

}
