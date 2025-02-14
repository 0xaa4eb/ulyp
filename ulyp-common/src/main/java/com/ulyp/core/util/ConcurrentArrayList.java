package com.ulyp.core.util;

import org.jetbrains.annotations.TestOnly;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class ConcurrentArrayList<V> {

    static final int CHUNK_SIZE_BITS;
    static final int CHUNK_SIZE;

    static {
        CHUNK_SIZE_BITS = Integer.parseInt(System.getProperty("ConcurrentArrayList.BITS", "15"));
        CHUNK_SIZE = 1 << CHUNK_SIZE_BITS;
    }

    private final AtomicReferenceArray<Chunk<V>> chunks;
    private final AtomicInteger chunksCount = new AtomicInteger(1);

    public ConcurrentArrayList() {
        this(64_000);
    }

    public ConcurrentArrayList(int chunksCapacity) {
        chunks = new AtomicReferenceArray<>(chunksCapacity);
        chunks.set(0, new Chunk<>());
    }

    public V get(int index) {
        int chunkIndex = index >> CHUNK_SIZE_BITS;
        int slot = index & (CHUNK_SIZE - 1);

        Chunk<V> chunk = chunks.get(chunkIndex);
        if (chunk == null) {
            return null;
        } else {
            return chunk.get(slot);
        }
    }

    public int add(V value) {

        for (; ; ) {
            int currentChunkIndex = chunksCount.get() - 1;

            Chunk<V> chunk = chunks.get(currentChunkIndex);

            int slotTaken = chunk.tryAdd(value);
            if (slotTaken >= 0) {

                return (currentChunkIndex << CHUNK_SIZE_BITS) | slotTaken;
            } else {

                // try allocate a new chunk
                int nextChunkIndex = currentChunkIndex + 1;
                if (chunks.get(nextChunkIndex) == null) {
                    chunks.compareAndSet(nextChunkIndex, null, new Chunk<>());
                }

                // update chunkCount even if other thread succeeded
                // set a new value only it's less than a current value, protect from long stalls like GC
                int newChunkCount = nextChunkIndex + 1;
                for (; ; ) {
                    int currentValue = chunksCount.get();
                    if (newChunkCount > currentValue) {
                        if (chunksCount.compareAndSet(currentValue, newChunkCount)) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
    }

    public int size() {
        for (; ; ) {
            int currentChunkIndex = chunksCount.get() - 1;
            Chunk<V> currentChunk = chunks.get(currentChunkIndex);
            if (currentChunk != null) {
                return currentChunkIndex * CHUNK_SIZE + currentChunk.size();
            }
        }
    }

    @TestOnly
    public boolean contains(V value) {
        int size = size();
        for (int i = 0; i < size; i++) {
            V valueAt = get(i);
            if (Objects.equals(valueAt, value)) {
                return true;
            }
        }
        return false;
    }

    private static class Chunk<V> {
        @SuppressWarnings("unchecked")
        private final V[] lookupCache = (V[]) new Object[CHUNK_SIZE];
        private final AtomicReferenceArray<V> values = new AtomicReferenceArray<>(CHUNK_SIZE);
        private final AtomicInteger nextSlot = new AtomicInteger(-1);

        public int tryAdd(V value) {
            int slot = this.nextSlot.incrementAndGet();

            if (slot >= 0 && slot < CHUNK_SIZE) {
                lookupCache[slot] = value;
                values.lazySet(slot, value);
                return slot;
            } else {
                return -1;
            }
        }

        public V get(int index) {
            // This is a benign data race, since value is read once
            // and values which are stored in this container are immutable and have primitive volatile fields
            // It gives 50% boost on some benchamrsk, so, even though the tricks is arguably unsafe, it's worth the price
            V v = lookupCache[index];
            if (v != null) {
                return v;
            }
            return values.get(index);
        }

        public int size() {
            int elementsTaken = nextSlot.get() + 1;
            return Math.min(elementsTaken, CHUNK_SIZE);
        }
    }
}
