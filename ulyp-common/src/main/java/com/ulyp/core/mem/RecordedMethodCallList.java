package com.ulyp.core.mem;

import com.ulyp.core.*;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.RecorderChooser;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import com.ulyp.core.recorders.bytes.EnterRecordSBEOutput;
import com.ulyp.core.recorders.bytes.ExitRecordSBEOutput;
import com.ulyp.core.util.BitUtil;
import com.ulyp.core.util.Preconditions;
import com.ulyp.transport.*;
import lombok.Getter;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A list of serialized {@link RecordedMethodCall} instances
 */
public class RecordedMethodCallList implements Iterable<RecordedMethodCall> {

    public static final byte ENTER_METHOD_CALL_ID = 1;
    public static final byte EXIT_METHOD_CALL_ID = 2;
    public static final int WIRE_ID = 2;

    @Getter
    private final int recordingId;
    private final EnterRecordSBEOutput enterRecordBinaryOutput = new EnterRecordSBEOutput();
    private final ExitRecordSBEOutput exitRecordBinaryOutput = new ExitRecordSBEOutput();
    private final BinaryRecordedEnterMethodCallEncoder enterMethodCallEncoder = new BinaryRecordedEnterMethodCallEncoder();
    private final BinaryRecordedExitMethodCallEncoder exitMethodCallEncoder = new BinaryRecordedExitMethodCallEncoder();
    private final BinaryList bytes;

    public RecordedMethodCallList(int recordingId) {
        this.bytes = new BinaryList(WIRE_ID);

        // store recordingId in the first entry
        byte[] buf = new byte[Long.BYTES];
        BitUtil.longToBytes(recordingId, buf, 0);
        this.bytes.add(buf);

        this.recordingId = recordingId;
    }

    public RecordedMethodCallList(BinaryList bytes) {
        Preconditions.checkArgument(bytes.id() == WIRE_ID, "Invalid binary list passed");
        this.bytes = bytes;

        BinaryDataDecoder firstEntry = bytes.iterator().next();
        byte[] buf = new byte[Long.BYTES];
        firstEntry.getValue(buf, 0, Long.BYTES);
        this.recordingId = (int) BitUtil.bytesToLong(buf, 0);
    }

    public void addExitMethodCall(int callId, TypeResolver typeResolver, Object returnValue) {
        addExitMethodCall(callId, typeResolver, false, returnValue, -1);
    }

    public void addExitMethodCall(int callId, TypeResolver typeResolver, Object returnValue, long nanoTime) {
        addExitMethodCall(callId, typeResolver, false, returnValue, nanoTime);
    }

    public void addExitMethodThrow(int callId, TypeResolver typeResolver, Object throwObject) {
        addExitMethodCall(callId, typeResolver, true, throwObject, -1L);
    }

    public void addExitMethodThrow(int callId, TypeResolver typeResolver, Object throwObject, long nanoTime) {
        addExitMethodCall(callId, typeResolver, true, throwObject, nanoTime);
    }

    private void addExitMethodCall(int callId, TypeResolver typeResolver, boolean thrown, Object returnValue, long nanoTime) {
        bytes.add(
                encoder -> {
                    MutableDirectBuffer wrappedBuffer = encoder.buffer();
                    encoder.id(EXIT_METHOD_CALL_ID);

                    int headerLength = 4;
                    int limit = encoder.limit();

                    exitMethodCallEncoder.wrap(wrappedBuffer, limit + headerLength);

                    exitMethodCallEncoder.callId(callId);
                    exitMethodCallEncoder.thrown(thrown ? BooleanType.T : BooleanType.F);
                    exitMethodCallEncoder.nanoTime(nanoTime);
                    Type type = typeResolver.get(returnValue);
                    exitMethodCallEncoder.returnValueTypeId(type.getId());

                    ObjectRecorder recorderHint = type.getRecorderHint();
                    if (returnValue != null && recorderHint == null) {
                        recorderHint = RecorderChooser.getInstance().chooseForType(returnValue.getClass());
                        type.setRecorderHint(recorderHint);
                    }

                    ObjectRecorder recorder = returnValue != null ?
                            (thrown ? ObjectRecorderRegistry.THROWABLE_RECORDER.getInstance() : recorderHint) :
                            ObjectRecorderRegistry.NULL_RECORDER.getInstance();

                    exitMethodCallEncoder.returnValueRecorderId(recorder.getId());
                    try (BinaryOutput output = exitRecordBinaryOutput.wrap(exitMethodCallEncoder)) {
                        recorder.write(returnValue, output, typeResolver);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    int typeSerializedLength = exitMethodCallEncoder.encodedLength();
                    encoder.limit(limit + headerLength + typeSerializedLength);
                    wrappedBuffer.putInt(limit, typeSerializedLength, java.nio.ByteOrder.LITTLE_ENDIAN);
                }
        );
    }

    public void addEnterMethodCall(int callId, Method method, TypeResolver typeResolver, Object callee, Object[] args) {
        addEnterMethodCall(callId, method, typeResolver, callee, args, -1L);
    }

    public void addEnterMethodCall(int callId, Method method, TypeResolver typeResolver, Object callee, Object[] args, long nanoTime) {
        bytes.add(
                encoder -> {
                    MutableDirectBuffer wrappedBuffer = encoder.buffer();
                    encoder.id(ENTER_METHOD_CALL_ID);

                    int headerLength = 4;
                    int limit = encoder.limit();

                    enterMethodCallEncoder.wrap(wrappedBuffer, limit + headerLength);

                    enterMethodCallEncoder.callId(callId);
                    enterMethodCallEncoder.methodId(method.getId());
                    enterMethodCallEncoder.nanoTime(nanoTime);

                    BinaryRecordedEnterMethodCallEncoder.ArgumentsEncoder argumentsEncoder = enterMethodCallEncoder.argumentsCount(args.length);

                    for (int i = 0; i < args.length; i++) {
                        Object argValue = args[i];
                        Type argType = typeResolver.get(argValue);
                        ObjectRecorder recorderHint = argType.getRecorderHint();
                        if (argValue != null && recorderHint == null) {
                            recorderHint = RecorderChooser.getInstance().chooseForType(argValue.getClass());
                            argType.setRecorderHint(recorderHint);
                        }

                        ObjectRecorder recorder = argValue != null ? recorderHint : ObjectRecorderRegistry.NULL_RECORDER.getInstance();

                        argumentsEncoder = argumentsEncoder.next();
                        argumentsEncoder.typeId(argType.getId());
                        argumentsEncoder.recorderId(recorder.getId());
                        try (BinaryOutput output = enterRecordBinaryOutput.wrap(enterMethodCallEncoder)) {
                            recorder.write(argValue, output, typeResolver);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    ObjectRecorder recorder = callee != null ? ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance() : ObjectRecorderRegistry.NULL_RECORDER.getInstance();

                    enterMethodCallEncoder.calleeTypeId(typeResolver.get(callee).getId());
                    enterMethodCallEncoder.calleeRecorderId(recorder.getId());
                    try (BinaryOutput output = enterRecordBinaryOutput.wrap(enterMethodCallEncoder)) {
                        recorder.write(callee, output, typeResolver);
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
        // one entry is always present for recordingId stored
        return bytes.size() - 1;
    }

    public int byteLength() {
        return bytes.byteLength();
    }

    public BinaryList getRawBytes() {
        return bytes;
    }

    public Stream<RecordedMethodCall> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
    }

    @NotNull
    @Override
    public AddressableItemIterator<RecordedMethodCall> iterator() {
        AddressableItemIterator<BinaryDataDecoder> iterator = bytes.iterator();
        iterator.next();

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
                if (decoder.id() == ENTER_METHOD_CALL_ID) {
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
