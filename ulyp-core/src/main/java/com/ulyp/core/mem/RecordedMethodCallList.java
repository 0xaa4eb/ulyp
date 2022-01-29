package com.ulyp.core.mem;

import com.google.common.base.Preconditions;
import com.ulyp.core.*;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderType;
import com.ulyp.core.recorders.bytes.BinaryOutputForEnterRecordImpl;
import com.ulyp.core.recorders.bytes.BinaryOutputForExitRecordImpl;
import com.ulyp.transport.*;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RecordedMethodCallList implements Iterable<RecordedMethodCall> {

    public static final int WIRE_ID = 2;

    private final BinaryOutputForEnterRecordImpl enterRecordBinaryOutput = new BinaryOutputForEnterRecordImpl();
    private final BinaryOutputForExitRecordImpl exitRecordBinaryOutput = new BinaryOutputForExitRecordImpl();
    private final BinaryRecordedEnterMethodCallEncoder enterMethodCallEncoder = new BinaryRecordedEnterMethodCallEncoder();
    private final BinaryRecordedExitMethodCallEncoder exitMethodCallEncoder = new BinaryRecordedExitMethodCallEncoder();
    private final BinaryList bytes;

    public RecordedMethodCallList() {
        bytes = new BinaryList(WIRE_ID);
    }

    public RecordedMethodCallList(BinaryList bytes) {
        Preconditions.checkArgument(bytes.id() == WIRE_ID, "Invalid binary list passed");
        this.bytes = bytes;
    }

    public void addExitMethodCall(
            int recordingId,
            long callId,
            Method method,
            TypeResolver typeResolver,
            boolean thrown,
            Object returnValue)
    {
        bytes.add(
                encoder -> {
                    MutableDirectBuffer wrappedBuffer = encoder.buffer();
                    encoder.id(BinaryRecordedExitMethodCallEncoder.TEMPLATE_ID);

                    int headerLength = 4;
                    int limit = encoder.limit();

                    exitMethodCallEncoder.wrap(wrappedBuffer, limit + headerLength);

                    exitMethodCallEncoder.recordingId(recordingId);
                    exitMethodCallEncoder.callId(callId);
                    exitMethodCallEncoder.methodId(method.getId());
                    exitMethodCallEncoder.thrown(thrown ? BooleanType.T : BooleanType.F);
                    Type classDescription = typeResolver.get(returnValue);
                    exitMethodCallEncoder.returnValueTypeId(classDescription.getId());

                    ObjectRecorder recorder = returnValue != null ?
                            (thrown ? ObjectRecorderType.THROWABLE_RECORDER.getInstance() : method.getReturnValueRecorder()) :
                            ObjectRecorderType.NULL_RECORDER.getInstance();

                    exitMethodCallEncoder.returnValueRecorderId(recorder.getId());
                    exitRecordBinaryOutput.wrap(exitMethodCallEncoder);
                    try {
                        recorder.write(returnValue, exitRecordBinaryOutput, typeResolver);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    int typeSerializedLength = exitMethodCallEncoder.encodedLength();
                    encoder.limit(limit + headerLength + typeSerializedLength);
                    wrappedBuffer.putInt(limit, typeSerializedLength, java.nio.ByteOrder.LITTLE_ENDIAN);
                }
        );
    }

    public void addEnterMethodCall(
            int recordingId,
            long callId,
            Method method,
            TypeResolver typeResolver,
            Object callee,
            Object[] args)
    {
        bytes.add(
                encoder -> {
                    MutableDirectBuffer wrappedBuffer = encoder.buffer();
                    encoder.id(BinaryRecordedEnterMethodCallEncoder.TEMPLATE_ID);

                    int headerLength = 4;
                    int limit = encoder.limit();

                    enterMethodCallEncoder.wrap(wrappedBuffer, limit + headerLength);

                    enterMethodCallEncoder.recordingId(recordingId);
                    enterMethodCallEncoder.callId(callId);
                    enterMethodCallEncoder.methodId(method.getId());
                    ObjectRecorder[] paramRecorders = method.getParameterRecorders();

                    BinaryRecordedEnterMethodCallEncoder.ArgumentsEncoder argumentsEncoder = enterMethodCallEncoder.argumentsCount(args.length);

                    for (int i = 0; i < args.length; i++) {
                        ObjectRecorder recorder = args[i] != null ? paramRecorders[i] : ObjectRecorderType.NULL_RECORDER.getInstance();

                        Type argType = typeResolver.get(args[i]);

                        argumentsEncoder = argumentsEncoder.next();
                        argumentsEncoder.typeId(argType.getId());
                        argumentsEncoder.recorderId(recorder.getId());
                        enterRecordBinaryOutput.wrap(enterMethodCallEncoder);
                        try {
                            recorder.write(args[i], enterRecordBinaryOutput, typeResolver);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    ObjectRecorder recorder = callee != null ? ObjectRecorderType.IDENTITY_RECORDER.getInstance() : ObjectRecorderType.NULL_RECORDER.getInstance();

                    enterMethodCallEncoder.calleeTypeId(typeResolver.get(callee).getId());
                    enterMethodCallEncoder.calleeRecorderId(recorder.getId());
                    enterRecordBinaryOutput.wrap(enterMethodCallEncoder);
                    try {
                        recorder.write(callee, enterRecordBinaryOutput, typeResolver);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    int typeSerializedLength = enterMethodCallEncoder.encodedLength();
                    encoder.limit(limit + headerLength + typeSerializedLength);
                    wrappedBuffer.putInt(limit, typeSerializedLength, java.nio.ByteOrder.LITTLE_ENDIAN);
                }
        );
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return bytes.size();
    }

    public BinaryList getRawBytes() {
        return bytes;
    }

    public AddressableItemIterator<BinaryDataDecoder> binaryIterator() {
        return bytes.iterator();
    }

    public Stream<RecordedMethodCall> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
    }

    @NotNull
    @Override
    public AddressableItemIterator<RecordedMethodCall> iterator() {
        AddressableItemIterator<BinaryDataDecoder> iterator = bytes.iterator();
        return new AddressableItemIterator<RecordedMethodCall>() {
            @Override
            public long address() {
                return iterator.address();
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public RecordedMethodCall next() {
                BinaryDataDecoder decoder = iterator.next();
                UnsafeBuffer buffer = new UnsafeBuffer();
                decoder.wrapValue(buffer);
                if (decoder.id() == BinaryRecordedEnterMethodCallEncoder.TEMPLATE_ID) {
                    BinaryRecordedEnterMethodCallDecoder enterMethodCallDecoder = new BinaryRecordedEnterMethodCallDecoder();
                    enterMethodCallDecoder.wrap(buffer, 0, BinaryRecordedEnterMethodCallEncoder.BLOCK_LENGTH, 0);
                    return RecordedEnterMethodCall.deserialize(enterMethodCallDecoder);
                } else {
                    BinaryRecordedExitMethodCallDecoder exitMethodCallDecoder = new BinaryRecordedExitMethodCallDecoder();
                    exitMethodCallDecoder.wrap(buffer, 0, BinaryRecordedExitMethodCallDecoder.BLOCK_LENGTH, 0);
                    return RecordedExitMethodCall.deserialize(exitMethodCallDecoder);
                }
            }
        };
    }
}
