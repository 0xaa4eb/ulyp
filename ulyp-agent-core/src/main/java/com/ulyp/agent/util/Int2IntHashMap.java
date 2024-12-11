package com.ulyp.agent.util;

import static com.ulyp.agent.util.HashCommon.arraySize;
import static com.ulyp.agent.util.HashCommon.maxFill;

/**
 * Copied from fastutil library to avoid adding a dependency to the agent.
 * Source: https://github.com/vigna/fastutil
 * License: Apache 2.0
 */
public class Int2IntHashMap {

    private static final int DEFAULT_INITIAL_SIZE = 16;
    private static final float DEFAULT_LOAD_FACTOR = .25f;
    private static final int DEFAULT_RETURN_VALUE = 0;

    private final float f;
    private int[] key;
    private int[] value;
    private boolean[] used;
    private int n;
    private int maxFill;
    private int mask;
    private int size;

    @SuppressWarnings("unchecked")
    public Int2IntHashMap(final int expected, final float f) {
        if (f <= 0 || f > 1)
            throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than or equal to 1");
        if (expected < 0) throw new IllegalArgumentException("The expected number of elements must be nonnegative");
        this.f = f;
        n = arraySize(expected, f);
        mask = n - 1;
        maxFill = maxFill(n, f);
        key = new int[n];
        value = new int[n];
        used = new boolean[n];
    }

    public Int2IntHashMap() {
        this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
    }

    public int put(final int k, final int v) {
        // The starting point.
        int pos = (HashCommon.murmurHash3((k))) & mask;
        // There's always an unused entry.
        while (used[pos]) {
            if (((key[pos]) == (k))) {
                final int oldValue = value[pos];
                value[pos] = v;
                return oldValue;
            }
            pos = (pos + 1) & mask;
        }
        used[pos] = true;
        key[pos] = k;
        value[pos] = v;
        if (++size >= maxFill) rehash(arraySize(size + 1, f));
        return DEFAULT_RETURN_VALUE;
    }

    public int addTo(final int k, final int incr) {
        // The starting point.
        int pos = (HashCommon.murmurHash3((k))) & mask;
        // There's always an unused entry.
        while (used[pos]) {
            if (((key[pos]) == (k))) {
                final int oldValue = value[pos];
                value[pos] += incr;
                return oldValue;
            }
            pos = (pos + 1) & mask;
        }
        used[pos] = true;
        key[pos] = k;
        value[pos] = DEFAULT_RETURN_VALUE + incr;
        if (++size >= maxFill) rehash(arraySize(size + 1, f));
        return DEFAULT_RETURN_VALUE;
    }

    protected final int shiftKeys(int pos) {
        // Shift entries with the same hash.
        int last;
        int slot;
        for (; ; ) {
            pos = ((last = pos) + 1) & mask;
            while (used[pos]) {
                slot = (HashCommon.murmurHash3((key[pos]))) & mask;
                if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) break;
                pos = (pos + 1) & mask;
            }
            if (!used[pos]) break;
            key[last] = key[pos];
            value[last] = value[pos];
        }
        used[last] = false;
        return last;
    }

    @SuppressWarnings("unchecked")
    public int remove(final int k) {
        // The starting point.
        int pos = (HashCommon.murmurHash3((k))) & mask;
        // There's always an unused entry.
        while (used[pos]) {
            if (((key[pos]) == (k))) {
                size--;
                final int v = value[pos];
                shiftKeys(pos);
                return v;
            }
            pos = (pos + 1) & mask;
        }
        return DEFAULT_RETURN_VALUE;
    }

    public Integer get(final Integer ok) {
        final int k = ((ok).intValue());
        // The starting point.
        int pos = (HashCommon.murmurHash3((k))) & mask;
        // There's always an unused entry.
        while (used[pos]) {
            if (((key[pos]) == (k))) return (Integer.valueOf(value[pos]));
            pos = (pos + 1) & mask;
        }
        return (null);
    }

    @SuppressWarnings("unchecked")
    public int get(final int k) {
        // The starting point.
        int pos = (HashCommon.murmurHash3((k))) & mask;
        // There's always an unused entry.
        while (used[pos]) {
            if (((key[pos]) == (k))) return value[pos];
            pos = (pos + 1) & mask;
        }
        return DEFAULT_RETURN_VALUE;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @SuppressWarnings("unchecked")
    protected void rehash(final int newN) {
        int i = 0, pos;
        final boolean[] used = this.used;
        int k;
        final int[] key = this.key;
        final int[] value = this.value;
        final int newMask = newN - 1;
        final int[] newKey = new int[newN];
        final int[] newValue = new int[newN];
        final boolean[] newUsed = new boolean[newN];
        for (int j = size; j-- != 0; ) {
            while (!used[i]) i++;
            k = key[i];
            pos = (HashCommon.murmurHash3((k))) & newMask;
            while (newUsed[pos]) pos = (pos + 1) & newMask;
            newUsed[pos] = true;
            newKey[pos] = k;
            newValue[pos] = value[i];
            i++;
        }
        n = newN;
        mask = newMask;
        maxFill = maxFill(n, f);
        this.key = newKey;
        this.value = newValue;
        this.used = newUsed;
    }
}
