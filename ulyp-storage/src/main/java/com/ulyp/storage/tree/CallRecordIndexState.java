package com.ulyp.storage.tree;

import lombok.*;
import org.agrona.collections.LongArrayList;

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
    private final LongArrayList childrenCallIds = new LongArrayList();
    @Builder.Default
    private int subtreeSize = 1;
    @Builder.Default
    @Setter
    private long exitMethodCallAddr = -1;

    public void incrementSubtreeSize() {
        subtreeSize++;
    }

    public void addChildrenCallId(long callId) {
        childrenCallIds.add(callId);
    }
}
