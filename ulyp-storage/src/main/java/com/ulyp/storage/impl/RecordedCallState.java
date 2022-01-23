package com.ulyp.storage.impl;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import lombok.Getter;

@Getter
public class RecordedCallState {

    private final long callId;
    private final long enterMethodCallAddr;
    private final LongList childrenCallIds = new LongArrayList();
    private int subtreeSize = 1;
    private long exitMethodCallAddr = -1;

    public RecordedCallState(long callId, long enterMethodCallAddr) {
        this.callId = callId;
        this.enterMethodCallAddr = enterMethodCallAddr;
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
}
