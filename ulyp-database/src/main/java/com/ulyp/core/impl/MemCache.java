package com.ulyp.core.impl;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayDeque;
import java.util.Deque;

public class MemCache {

    private final Deque<CallRecordIndexMetadata> fromRootPath = new ArrayDeque<>();
    private final Long2ObjectMap<CallRecordIndexMetadata> lookupIndex = new Long2ObjectOpenHashMap<>();

    public CallRecordIndexMetadata find(long callId) {
        return lookupIndex.get(callId);
    }

    public void insert(CallRecordIndexMetadata callRecordIndexMetadata) {
        if (!isEmpty()) {

            for (CallRecordIndexMetadata state : fromRootPath) {
                state.incSubtreeCount();
            }

            CallRecordIndexMetadata parent = fromRootPath.getLast();
            parent.getChildren().add(callRecordIndexMetadata.getCallId());
        }

        this.fromRootPath.add(callRecordIndexMetadata);
        this.lookupIndex.put(callRecordIndexMetadata.getCallId(), callRecordIndexMetadata);
    }

    public long lastCallId() {
        return fromRootPath.getLast().getCallId();
    }

    public int size() {
        return fromRootPath.size();
    }

    public CallRecordIndexMetadata popLast() {
        CallRecordIndexMetadata popped = fromRootPath.removeLast();
        lookupIndex.put(popped.getCallId(), null);
        return popped;
    }

    public boolean isEmpty() {
        return fromRootPath.isEmpty();
    }


}
