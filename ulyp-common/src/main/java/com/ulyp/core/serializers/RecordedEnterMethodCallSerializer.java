package com.ulyp.core.serializers;

import com.ulyp.core.RecordedEnterMethodCall;
import com.ulyp.core.Type;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.repository.ReadableRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecordedEnterMethodCallSerializer {

    public static final RecordedEnterMethodCallSerializer instance = new RecordedEnterMethodCallSerializer();

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
