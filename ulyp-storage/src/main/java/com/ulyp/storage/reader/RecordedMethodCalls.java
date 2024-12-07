package com.ulyp.storage.reader;

import com.ulyp.core.AddressableItemIterator;
import com.ulyp.core.RecordedMethodCall;
import com.ulyp.core.Type;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.mem.InputBytesList;
import com.ulyp.core.mem.SerializedRecordedMethodCallList;
import com.ulyp.core.repository.ReadableRepository;
import com.ulyp.core.serializers.RecordedEnterMethodCallSerializer;
import com.ulyp.core.serializers.RecordedExitMethodCallSerializer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class RecordedMethodCalls {

    private final InputBytesList bytesIn;
    @Getter
    private final int recordingId;

    public RecordedMethodCalls(InputBytesList bytesIn) {
        this.bytesIn = bytesIn;
        if (bytesIn.id() != SerializedRecordedMethodCallList.WIRE_ID) {
            throw new IllegalArgumentException("Invalid wire id");
        }
        BytesIn firstEntry = bytesIn.iterator().next();
        this.recordingId = firstEntry.readInt();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return bytesIn.size() - 1;
    }

    @NotNull
    public AddressableItemIterator<RecordedMethodCall> iterator(ReadableRepository<Integer, Type> typeResolver) {
        AddressableItemIterator<BytesIn> iterator = bytesIn.iterator();
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
                BytesIn in = iterator.next();
                if (in.readByte() == SerializedRecordedMethodCallList.ENTER_METHOD_CALL_ID) {
                    return RecordedEnterMethodCallSerializer.deserialize(in, typeResolver);
                } else {
                    return RecordedExitMethodCallSerializer.deserialize(in, typeResolver);
                }
            }
        };
    }
}
