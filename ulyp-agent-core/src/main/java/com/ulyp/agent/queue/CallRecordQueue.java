package com.ulyp.agent.queue;

import org.jetbrains.annotations.Nullable;

import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.ulyp.agent.RecordDataWriter;
import com.ulyp.core.RecordingMetadata;

public class CallRecordQueue {

    private final Disruptor<CallRecordQueueItemHolder> disruptor;

    public CallRecordQueue(RecordDataWriter recordDataWriter) {
        this.disruptor = new Disruptor<>(
            CallRecordQueueItemHolder::new,
            256 * 1024,
            DaemonThreadFactory.INSTANCE,
            ProducerType.MULTI,
            new SleepingWaitStrategy()
        );
        disruptor.start();
    }

    public void enqueueMethodEnter(RecordingMetadata recordingMetadata, int callId, int methodId, @Nullable Object callee, Object[] args, long nanoTime) {
        disruptor.publishEvent((entry, seq) -> entry.item = new EnterRecordQueueItem(recordingMetadata, callId, methodId, callee, args, nanoTime));
    }

    public void enqueueMethodExit(int recordingId, int callId, Object returnValue, boolean thrown) {
        disruptor.publishEvent((entry, seq) -> entry.setExitCall(recordingId, callId, returnValue, thrown));
    }

}
