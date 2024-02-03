package com.ulyp.storage.reader;

import com.ulyp.core.AddressableItemIterator;
import com.ulyp.core.RecordedMethodCall;
import com.ulyp.core.Type;
import com.ulyp.core.mem.BinaryList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.bytes.BinaryInput;
import com.ulyp.core.repository.ReadableRepository;
import com.ulyp.core.serializers.RecordedEnterMethodCallSerializer;
import com.ulyp.core.serializers.RecordedExitMethodCallSerializer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class RecordedMethodCalls {

    private final BinaryList.In bytesIn;
    @Getter
    private final int recordingId;

    public RecordedMethodCalls(BinaryList.In bytesIn) {
        this.bytesIn = bytesIn;
        if (bytesIn.id() != RecordedMethodCallList.WIRE_ID) {
            throw new IllegalArgumentException("Invalid wire id");
        }
        BinaryInput firstEntry = bytesIn.iterator().next();
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
        AddressableItemIterator<BinaryInput> iterator = bytesIn.iterator();
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
                if (in.readByte() == RecordedMethodCallList.ENTER_METHOD_CALL_ID) {
                    return RecordedEnterMethodCallSerializer.deserialize(in, typeResolver);
                } else {
                    return RecordedExitMethodCallSerializer.deserialize(in, typeResolver);
                }
            }
        };
    }
}
