package com.ulyp.core.impl;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

public class CallRecordIndexMetadata {

    private final long callId;
    private final long enterAddressPos;
    private long exitAddressPos = -1;
    private final LongList children = new LongArrayList();
    private long subtreeCount = 1;

    public CallRecordIndexMetadata(long callId, long enterAddressPos) {
        this.callId = callId;
        this.enterAddressPos = enterAddressPos;
    }


    public long getCallId() {
        return callId;
    }

    public long getEnterAddressPos() {
        return enterAddressPos;
    }

    public long getExitAddressPos() {
        return exitAddressPos;
    }

    public LongList getChildren() {
        return children;
    }

    public long getSubtreeCount() {
        return subtreeCount;
    }

    public CallRecordIndexMetadata setExitAddressPos(long exitAddressPos) {
        this.exitAddressPos = exitAddressPos;
        return this;
    }

    public void incSubtreeCount() {
        subtreeCount++;
    }
}
