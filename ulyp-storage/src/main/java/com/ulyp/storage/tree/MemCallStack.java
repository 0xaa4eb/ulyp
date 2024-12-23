package com.ulyp.storage.tree;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Maintains a call stack for each recording being processed. Having in-memory stack
 * allows updating total call count for every parent call. Once the index state is popped, it's no
 * long needed to be updated and can be safely stored to index.
 * The call stack is always queried first before querying the index.
 */
public class MemCallStack {

    private final Deque<CallRecordIndexState> deque = new ArrayDeque<>();

    // We don't optimize boxing, it's used in UI app
    private final Map<Long, CallRecordIndexState> lookupIndex = new HashMap<>();

    public CallRecordIndexState get(long callId) {
        return lookupIndex.get(callId);
    }

    public void push(CallRecordIndexState value) {
        for (CallRecordIndexState state : deque) {
            state.incrementSubtreeSize();
        }

        if (!deque.isEmpty()) {
            deque.getLast().addChildrenCallId(value.getId());
        }

        deque.add(value);
        lookupIndex.put(value.getId(), value);
    }

    public void pop() {
        CallRecordIndexState top = deque.removeLast();
        lookupIndex.remove(top.getId());
    }

    @Nullable
    public CallRecordIndexState peek() {
        return deque.peekLast();
    }
}
