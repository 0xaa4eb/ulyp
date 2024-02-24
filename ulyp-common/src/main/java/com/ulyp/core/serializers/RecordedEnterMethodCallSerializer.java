package com.ulyp.core.serializers;

import com.ulyp.core.RecordedEnterMethodCall;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.exception.RecordingException;
import com.ulyp.core.recorders.*;
import com.ulyp.core.bytes.BinaryInput;
import com.ulyp.core.bytes.BinaryOutput;
import com.ulyp.core.recorders.*;
import com.ulyp.core.bytes.BinaryInput;
import com.ulyp.core.bytes.BinaryOutput;
import com.ulyp.core.repository.ReadableRepository;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecordedEnterMethodCallSerializer {

    public static final RecordedEnterMethodCallSerializer instance = new RecordedEnterMethodCallSerializer();

    public static final byte ENTER_METHOD_CALL_ID = 1;

    public void serializeEnterMethodCall(BinaryOutput out, int callId, int methodId, TypeResolver typeResolver, Object callee, Object[] args, long nanoTime) {
        out.write(ENTER_METHOD_CALL_ID);
        out.write(callId);
        out.write(methodId);
        out.write(nanoTime);
        serializeArgs(out, typeResolver, args);
        serializeCallee(out, typeResolver, callee);
    }

    private static void serializeCallee(BinaryOutput out, TypeResolver typeResolver, Object callee) {
        if (callee != null) {

            ObjectRecorder recorder = callee instanceof QueuedIdentityObject ? ObjectRecorderRegistry.QUEUE_IDENTITY_RECORDER.getInstance() : ObjectRecorderRegistry.IDENTITY_RECORDER.getInstance();

            out.write(typeResolver.get(callee).getId());
            out.write(recorder.getId());
            try {
                recorder.write(callee, out, typeResolver);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            ObjectRecorder recorder = ObjectRecorderRegistry.NULL_RECORDER.getInstance();
            out.write(Type.unknown().getId());
            out.write(recorder.getId());
            try {
                recorder.write(null, out, typeResolver);
            } catch (Exception e) {
                throw new RecordingException("Error while serializing callee", e);
            }
        }
    }

    private static void serializeArgs(BinaryOutput out, TypeResolver typeResolver, Object[] args) {
        out.write(args.length);
        for (int argIndex = 0; argIndex < args.length; argIndex++) {
            Object argValue = args[argIndex];
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
                throw new RecordingException("Error while serializing argument at index " + argIndex, e);
            }
        }
    }

    private static ObjectRecord deserializeObject(BinaryInput input, ReadableRepository<Integer, Type> typeResolver) {
        int typeId = input.readInt();
        byte recorderId = input.readByte();
        Type type = Optional.ofNullable(typeResolver.get(typeId)).orElse(Type.unknown());
        ObjectRecorder objectRecorder = ObjectRecorderRegistry.recorderForId(recorderId);
        return objectRecorder.read(
                type,
                input,
                id -> Optional.ofNullable(typeResolver.get(id)).orElse(Type.unknown())
        );
    }

    public static RecordedEnterMethodCall deserialize(BinaryInput input, ReadableRepository<Integer, Type> typeResolver) {
        int callId = input.readInt();
        int methodId = input.readInt();
        long nanoTime = input.readLong();
        int argsCount = input.readInt();

        List<ObjectRecord> arguments = new ArrayList<>(argsCount);

        for (int i = 0; i < argsCount; i++) {
            arguments.add(deserializeObject(input, typeResolver));
        }

        ObjectRecord callee = deserializeObject(input, typeResolver);

        return RecordedEnterMethodCall.builder()
                .callId(callId)
                .methodId(methodId)
                .nanoTime(nanoTime)
                .callee(callee)
                .arguments(arguments)
                .build();
    }
}
