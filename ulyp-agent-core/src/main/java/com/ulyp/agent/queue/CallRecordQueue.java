package com.ulyp.agent.queue;

import org.jetbrains.annotations.Nullable;

import com.lmax.disruptor.BlockingWaitStrategy;
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

    public CallRecordQueue(TypeResolver typeResolver, RecordDataWriter recordDataWriter) {
        this.typeResolver = typeResolver;
        this.disruptor = new Disruptor<>(
            CallRecordQueueItemHolder::new,
            256 * 1024,
            new CallRecordQueueProcessorThreadFactory(),
            ProducerType.MULTI,
            new BlockingWaitStrategy()
        );
        disruptor.handleEventsWith(new CallRecordQueueProcessor(typeResolver, recordDataWriter));
        disruptor.start();
    }

    public void enqueueMethodEnter(RecordingMetadata recordingMetadata, int callId, int methodId, @Nullable Object callee, Object[] args, long nanoTime) {
        disruptor.publishEvent((entry, seq) -> entry.item = new EnterRecordQueueItem(recordingMetadata, callId, methodId, convert(callee), convert(args), nanoTime));
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
}
