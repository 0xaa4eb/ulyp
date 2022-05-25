package com.ulyp.core;

import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectRecorderRegistry;
import com.ulyp.core.recorders.bytes.BinaryInputImpl;
import com.ulyp.core.repository.ReadableRepository;
import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@Builder
@Getter
public class RecordedObject {

    private final byte recorderId;
    private final long typeId;
    private final byte[] value;

    public ObjectRecord toRecord(ReadableRepository<Long, Type> typeResolver) {

        Type type = Optional.ofNullable(typeResolver.get(typeId)).orElse(Type.unknown());
        ObjectRecorder objectRecorder = ObjectRecorderRegistry.recorderForId(recorderId);
        return objectRecorder.read(
                type,
                new BinaryInputImpl(value),
                id -> Optional.ofNullable(typeResolver.get(id)).orElse(Type.unknown())
        );
    }
}
