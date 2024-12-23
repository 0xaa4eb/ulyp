package com.ulyp.storage.tree;

import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.serializers.Serializer;
import org.agrona.collections.LongArrayList;

public class BinaryRecordedCallStateSerializer implements Serializer<CallRecordIndexState> {

    public static final Serializer<CallRecordIndexState> instance = new BinaryRecordedCallStateSerializer();

    @Override
    public CallRecordIndexState deserialize(BytesIn input) {
        long id = input.readLong();
        long enterCallRecordAddress = input.readLong();
        int subtreeSize = input.readInt();
        long exitCallRecordAddress = input.readLong();
        int childrenCallCount = input.readInt();
        LongArrayList childrenCallIds = new LongArrayList(childrenCallCount, Long.MIN_VALUE);
        for (int i = 0; i < childrenCallCount; i++) {
            childrenCallIds.add(input.readLong());
        }
        return CallRecordIndexState.builder()
                .id(id)
                .enterMethodCallAddress(enterCallRecordAddress)
                .subtreeSize(subtreeSize)
                .exitMethodCallAddr(exitCallRecordAddress)
                .childrenCallIds(childrenCallIds)
                .build();
    }

    @Override
    public void serialize(BytesOut out, CallRecordIndexState value) {
        out.write(value.getId());
        out.write(value.getEnterMethodCallAddress());
        out.write(value.getSubtreeSize());
        out.write(value.getExitMethodCallAddr());
        LongArrayList childrenCallIds = value.getChildrenCallIds();
        int childrenCallIdCount = childrenCallIds.size();
        out.write(childrenCallIdCount);
        for (int i = 0; i < childrenCallIdCount; i++) {
            out.write(childrenCallIds.getLong(i));
        }
    }
}
