package com.ulyp.agent.util;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.TestOnly;

import java.util.List;

/**
 * A stack which can contain ints. Supports fast lookup.
 * If the number of elements is small (that's what we expect), we use linear search.
 * If the number is greater than some threshold, a hash map is used for lookup.
 */
@Slf4j
public class FastLookupIntStack {

    private static final int HASH_MAP_LOOKUP_MIN_SIZE = 4;

    private final IntStack stack = new IntStack();
    private final Int2IntHashMap lookupIndex = new Int2IntHashMap();

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public boolean contains(int value) {
        if (stack.size() < HASH_MAP_LOOKUP_MIN_SIZE) {
            return stack.contains(value);
        } else {
            return lookupIndex.get(value) > 0;
        }
    }

    public void push(int value) {
        stack.push(value);
        lookupIndex.addTo(value, 1);
    }

    public int top() {
        return stack.top();
    }

    /**
     * Pops everything until finds a provided value. The found value is also popped.
     *
     * @param value a value to search for
     */
    public void popUpTo(int value) {
        while (!stack.isEmpty()) {
            int next = stack.top();
            pop();
            if (next == value) {
                return;
            }
        }
    }

    /**
     * Pops if top matches the provided value.
     */
    public boolean popIfTop(int expectedTop) {
        if (stack.popIfTop(expectedTop)) {
            removeFromIndex(expectedTop);
            return true;
        }
        return false;
    }

    public int pop() {
        int value = stack.pop();
        removeFromIndex(value);
        return value;
    }

    private void removeFromIndex(int value) {
        int cnt = lookupIndex.get(value);
        if (cnt == 1) {
            lookupIndex.remove(value);
        } else {
            lookupIndex.put(value, cnt - 1);
        }
    }

    @TestOnly
    public List<Integer> toList() {
        return stack.toList();
    }
}
