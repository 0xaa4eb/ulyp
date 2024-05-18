package com.ulyp.storage.tree;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CallRecordIndexState {

    // uniqueId is combined from both recordingId and callId and used as PK in Rocksdb index
    private final long id;
    private final long enterMethodCallAddress;
    @Builder.Default
    private final LongList childrenCallIds = new LongArrayList();
    @Builder.Default
    private int subtreeSize = 1;
    @Builder.Default
    private long exitMethodCallAddr = -1;

    public void incrementSubtreeSize() {
        subtreeSize++;
    }

    public void addChildrenCallId(long callId) {
        childrenCallIds.add(callId);
    }

    public void setExitMethodCallAddr(long value) {
        this.exitMethodCallAddr = value;
    }
}
