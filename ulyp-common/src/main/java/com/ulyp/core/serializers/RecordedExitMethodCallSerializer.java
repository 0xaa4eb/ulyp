package com.ulyp.core.serializers;

import com.ulyp.core.RecordedExitMethodCall;
import com.ulyp.core.Type;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.repository.ReadableRepository;

import java.util.Optional;

public class RecordedExitMethodCallSerializer {

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

    public static RecordedExitMethodCall deserialize(BinaryInput input, ReadableRepository<Integer, Type> typeResolver) {

        int callId = input.readInt();
        boolean thrown = input.readBoolean();
        long nanoTime = input.readLong();

        return RecordedExitMethodCall.builder()
                .returnValue(deserializeObject(input, typeResolver))
                .thrown(thrown)
                .callId(callId)
                .nanoTime(nanoTime)
                .build();
    }
}
