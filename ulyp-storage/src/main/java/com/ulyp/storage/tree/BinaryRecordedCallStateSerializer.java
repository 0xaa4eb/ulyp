package com.ulyp.storage.tree;

import com.ulyp.core.bytes.BinaryInput;
import com.ulyp.core.bytes.BinaryOutput;
import com.ulyp.core.serializers.Serializer;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

public class BinaryRecordedCallStateSerializer implements Serializer<CallRecordIndexState> {

    public static final Serializer<CallRecordIndexState> instance = new BinaryRecordedCallStateSerializer();

    @Override
    public CallRecordIndexState deserialize(BinaryInput input) {
        long id = input.readLong();
        long enterCallRecordAddress = input.readLong();
        int subtreeSize = input.readInt();
        long exitCallRecordAddress = input.readLong();
        int childrenCallCount = input.readInt();
        LongList childrenCallIds = new LongArrayList(childrenCallCount);
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
    public void serialize(BinaryOutput out, CallRecordIndexState value) {
        out.write(value.getId());
        out.write(value.getEnterMethodCallAddress());
        out.write(value.getSubtreeSize());
        out.write(value.getExitMethodCallAddr());
        LongList childrenCallIds = value.getChildrenCallIds();
        int childrenCallIdCount = childrenCallIds.size();
        out.write(childrenCallIdCount);
        for (int i = 0; i < childrenCallIdCount; i++) {
            out.write(childrenCallIds.getLong(i));
        }
    }
}
