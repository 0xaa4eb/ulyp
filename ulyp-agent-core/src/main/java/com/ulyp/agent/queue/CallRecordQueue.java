package com.ulyp.agent.queue;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

import org.jetbrains.annotations.Nullable;

import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.ulyp.agent.RecordDataWriter;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.CallRecordQueueIdentityObject;
import com.ulyp.core.recorders.IdentityRecorder;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.RecorderChooser;

public class CallRecordQueue {

    private final TypeResolver typeResolver;
    private final Disruptor<CallRecordQueueItemHolder> disruptor;
    private final CallRecordQueueProcessor queueProcessor;

    public CallRecordQueue(TypeResolver typeResolver, RecordDataWriter recordDataWriter) {
        this.typeResolver = typeResolver;
        this.disruptor = new Disruptor<>(
            CallRecordQueueItemHolder::new,
            256 * 1024, // TODO configurable
            new CallRecordQueueProcessorThreadFactory(),
            ProducerType.MULTI,
            new SleepingWaitStrategy()
        );
        this.queueProcessor = new CallRecordQueueProcessor(typeResolver, recordDataWriter);
    }

    public void start() {
        this.disruptor.handleEventsWith(queueProcessor);
        this.disruptor.start();
    }

    public void enqueueRecordingMetadataUpdate(RecordingMetadata recordingMetadata) {
        disruptor.publishEvent((entry, seq) -> entry.item = new UpdateRecordingMetadataQueueItem(recordingMetadata));
    }

    public void enqueueMethodEnter(int recordingId, int callId, int methodId, @Nullable Object callee, Object[] args, long nanoTime) {
        disruptor.publishEvent((entry, seq) -> entry.item = new EnterRecordQueueItem(recordingId, callId, methodId, convert(callee), convert(args), nanoTime));
    }

    public void enqueueMethodExit(int recordingId, int callId, Object returnValue, boolean thrown, long nanoTime) {
        disruptor.publishEvent((entry, seq) -> entry.item = new ExitRecordQueueItem(recordingId, callId, convert(returnValue), thrown, nanoTime));
    }

    private Object[] convert(Object[] args) {
        Object[] converted = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            converted[i] = convert(args[i]);
        }
        return converted;
    }

    private Object convert(Object value) {
        Type type = typeResolver.get(value);
        ObjectRecorder recorder = type.getRecorderHint();
        if (value != null && recorder == null) {
            recorder = RecorderChooser.getInstance().chooseForType(value.getClass());
            type.setRecorderHint(recorder);
        }
        if (value == null || recorder.supportsAsyncRecording()) {
            if (value != null && recorder instanceof IdentityRecorder) {
                return new CallRecordQueueIdentityObject(type, value);
            } else {
                return value;
            }
        } else {
            throw new RuntimeException("not supported yet");
        }
    }

    public void sync(Duration duration) throws InterruptedException, TimeoutException {
        long lastPublishedSeq = disruptor.getCursor();
        long deadlineWaitTimeMs = System.currentTimeMillis() + duration.toMillis();
        while (System.currentTimeMillis() < deadlineWaitTimeMs) {
            if (queueProcessor.getLastProcessedSeq() >= lastPublishedSeq) {
                return;
            }
            LockSupport.parkNanos(TimeUnit.MICROSECONDS.toNanos(10));
        }
        throw new TimeoutException("Timed out waiting cakk record queue flush. " +
            "Waiting for seq " + lastPublishedSeq + " to be processed. Event handler prcoessed "
            + queueProcessor.getLastProcessedSeq());
    }
}
