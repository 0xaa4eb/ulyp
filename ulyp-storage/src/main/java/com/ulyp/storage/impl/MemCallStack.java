package com.ulyp.storage.impl;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;

public class MemCallStack {

    private final Deque<RecordedCallState> deque = new ArrayDeque<>();
    private final Long2ObjectMap<RecordedCallState> lookupIndex = new Long2ObjectOpenHashMap<>();

    public RecordedCallState get(long callId) {
        return lookupIndex.get(callId);
    }

    public void push(RecordedCallState value) {
        for (RecordedCallState state : deque) {
            state.incrementSubtreeSize();
        }

        if (!deque.isEmpty()) {
            deque.getLast().addChildrenCallId(value.getCallId());
        }

        deque.add(value);
        lookupIndex.put(value.getCallId(), value);
    }

    public void pop() {
        RecordedCallState top = deque.removeLast();
        lookupIndex.remove(top.getCallId());
    }

    @Nullable
    public RecordedCallState peek() {
        return deque.peekLast();
    }
}
