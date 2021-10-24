package com.ulyp.core.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class ConcurrentArrayBasedMap<V> {

    private static final int CHUNK_SIZE_BITS;
    private static final int CHUNK_SIZE;

    static {
        CHUNK_SIZE_BITS = Integer.parseInt(System.getProperty("ConcurrentArrayBasedMap.BITS", "15"));
        CHUNK_SIZE = 1 << CHUNK_SIZE_BITS;
    }

    private final AtomicReferenceArray<Chunk<V>> chunks;
    private final AtomicInteger chunksCount = new AtomicInteger(1);

    public ConcurrentArrayBasedMap(int chunksCapacity) {
        chunks = new AtomicReferenceArray<>(chunksCapacity);
        chunks.set(0, new Chunk<>());
    }

    private static class Chunk<V> {
        private final AtomicReferenceArray<V> values = new AtomicReferenceArray<>(CHUNK_SIZE);
        private final AtomicInteger nextSlot = new AtomicInteger(-1);

        public int tryPut(V value) {
            int slot = this.nextSlot.incrementAndGet();

            if (slot >= 0 && slot < CHUNK_SIZE) {
                values.lazySet(slot, value);
                return slot;
            } else {
                return -1;
            }
        }

        public V get(int key) {
            return values.get(key);
        }
    }

    public V get(int key) {
        int chunkIndex = key >> CHUNK_SIZE_BITS;
        int slot = key & (CHUNK_SIZE - 1);

        Chunk<V> chunk = chunks.get(chunkIndex);
        if (chunk == null) {
            return null;
        } else {
            return chunk.get(slot);
        }
    }

    public int put(V value) {

        for (;;) {
            int currentChunkIndex = chunksCount.get() - 1;

            Chunk<V> chunk = chunks.get(currentChunkIndex);

            int slotTaken = chunk.tryPut(value);
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
                for(;;) {
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

    public static void main(String[] args) {
        System.out.println(CHUNK_SIZE);
        System.out.println(CHUNK_SIZE_BITS);
    }
}
