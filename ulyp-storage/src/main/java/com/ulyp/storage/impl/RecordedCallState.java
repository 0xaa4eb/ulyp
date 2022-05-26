package com.ulyp.storage.impl;

import com.ulyp.transport.BinaryRecordedCallStateDecoder;
import com.ulyp.transport.BinaryRecordedCallStateEncoder;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class RecordedCallState {

    private final long callId;
    private final long enterMethodCallAddr;
    @Builder.Default
    private final LongList childrenCallIds = new LongArrayList();
    @Builder.Default
    private int subtreeSize = 1;
    @Builder.Default
    private long exitMethodCallAddr = -1;

    public static RecordedCallState deserialize(BinaryRecordedCallStateDecoder decoder) {

        BinaryRecordedCallStateDecoder.ChildrenCallIdsDecoder childrenCallIdsDecoder = decoder.childrenCallIds();
        LongList childrenCallIds = new LongArrayList(childrenCallIdsDecoder.count());
        for (int i = 0; i < childrenCallIdsDecoder.count(); i++) {
            childrenCallIds.add(childrenCallIdsDecoder.next().callId());
        }

        return RecordedCallState.builder()
                .callId(decoder.callId())
                .enterMethodCallAddr(decoder.enterMethodCallAddr())
                .subtreeSize(decoder.subtreeSize())
                .exitMethodCallAddr(decoder.exitMethodCallAddr())
                .childrenCallIds(childrenCallIds)
                .build();
    }

    public void incrementSubtreeSize() {
        subtreeSize++;
    }

    public void addChildrenCallId(long callId) {
        childrenCallIds.add(callId);
    }

    public void setExitMethodCallAddr(long value) {
        this.exitMethodCallAddr = value;
    }

    public void serialize(BinaryRecordedCallStateEncoder encoder) {
        encoder.callId(callId);
        encoder.enterMethodCallAddr(enterMethodCallAddr);
        encoder.subtreeSize(subtreeSize);
        encoder.exitMethodCallAddr(exitMethodCallAddr);

        BinaryRecordedCallStateEncoder.ChildrenCallIdsEncoder childrenCallIdsEncoder = encoder.childrenCallIdsCount(childrenCallIds.size());
        for (int i = 0; i < childrenCallIds.size(); i++) {
            childrenCallIdsEncoder.next().callId(childrenCallIds.getLong(i));
        }
    }
}
