package com.ulyp.core.mem;

import com.ulyp.core.*;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.RecorderChooser;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BufferBinaryOutput;
import com.ulyp.core.repository.ReadableRepository;
import com.ulyp.core.serializers.RecordedEnterMethodCallSerializer;
import com.ulyp.core.serializers.RecordedExitMethodCallSerializer;
import com.ulyp.core.util.Preconditions;
import lombok.Getter;
import org.agrona.ExpandableDirectByteBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * A list of serialized {@link RecordedMethodCall} instances
 */
public class RecordedMethodCallList {

    public static final byte ENTER_METHOD_CALL_ID = 1;
    public static final byte EXIT_METHOD_CALL_ID = 2;
    public static final int WIRE_ID = 2;

    @Getter
    private final int recordingId;
    private WriteBinaryList writeBinaryList;
    private ReadBinaryList readBinaryList;

    public RecordedMethodCallList(int recordingId, WriteBinaryList writeBinaryList) {
        this.writeBinaryList = writeBinaryList;

        writeBinaryList.add(out -> out.write(recordingId));
        this.recordingId = recordingId;
    }

    public RecordedMethodCallList(int recordingId) {
        this.writeBinaryList = new WriteBinaryList(WIRE_ID, new BufferBinaryOutput(new ExpandableDirectByteBuffer()));

        writeBinaryList.add(out -> out.write(recordingId));
        this.recordingId = recordingId;
    }

    public RecordedMethodCallList(ReadBinaryList readBinaryList) {
        this.readBinaryList = readBinaryList;
        Preconditions.checkArgument(readBinaryList.id() == WIRE_ID, "Invalid binary list passed");

        BinaryInput next = readBinaryList.iterator().next();
        this.recordingId = next.readInt();
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
        writeBinaryList.add(out -> {
            out.write(EXIT_METHOD_CALL_ID);
            out.write(callId);
            out.write(thrown);
            out.write(nanoTime);

            Type type = typeResolver.get(returnValue);
            out.write(type.getId());

            ObjectRecorder recorderHint = type.getRecorderHint();
            if (returnValue != null && recorderHint == null) {
                recorderHint = RecorderChooser.getInstance().chooseForType(returnValue.getClass());
                type.setRecorderHint(recorderHint);
            }

            ObjectRecorder recorder = returnValue != null ?
                    (thrown ? ObjectRecorderRegistry.THROWABLE_RECORDER.getInstance() : recorderHint) :
                    ObjectRecorderRegistry.NULL_RECORDER.getInstance();

            out.write(recorder.getId());

            try {
                recorder.write(returnValue, out, typeResolver);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void addEnterMethodCall(int callId, Method method, TypeResolver typeResolver, Object callee, Object[] args) {
        addEnterMethodCall(callId, method, typeResolver, callee, args, -1L);
    }

    public void addEnterMethodCall(int callId, Method method, TypeResolver typeResolver, Object callee, Object[] args, long nanoTime) {
        writeBinaryList.add(out -> {
            out.write(ENTER_METHOD_CALL_ID);

            out.write(callId);
            out.write(method.getId());
            out.write(nanoTime);
            out.write(args.length);

            for (int i = 0; i < args.length; i++) {
                Object argValue = args[i];
                Type argType = typeResolver.get(argValue);
                ObjectRecorder recorderHint = argType.getRecorderHint();
                if (argValue != null && recorderHint == null) {
                    recorderHint = RecorderChooser.getInstance().chooseForType(argValue.getClass());
                    argType.setRecorderHint(recorderHint);
                }

                ObjectRecorder recorder = argValue != null ? recorderHint : ObjectRecorderRegistry.NULL_RECORDER.getInstance();

                out.write(argType.getId());
                out.write(recorder.getId());
                try {
                    recorder.write(argValue, out, typeResolver);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            ObjectRecorder recorder = callee != null ? ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance() : ObjectRecorderRegistry.NULL_RECORDER.getInstance();

            out.write(typeResolver.get(callee).getId());
            out.write(recorder.getId());
            try {
                recorder.write(callee, out, typeResolver);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        // one entry is always present for recordingId stored
        return readBinaryList.size() - 1;
    }

    @NotNull
    public AddressableItemIterator<RecordedMethodCall> iterator(ReadableRepository<Integer, Type> typeResolver) {
        AddressableItemIterator<BinaryInput> iterator = readBinaryList.iterator();
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
                BinaryInput in = iterator.next();
                if (in.readByte() == ENTER_METHOD_CALL_ID) {
                    return RecordedEnterMethodCallSerializer.deserialize(in, typeResolver);
                } else {
                    return RecordedExitMethodCallSerializer.deserialize(in, typeResolver);
                }
            }
        };
    }
}
