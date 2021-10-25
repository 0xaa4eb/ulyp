package com.ulyp.core.util;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConcurrentArrayBasedMapTest {

    static {
        System.setProperty("ConcurrentArrayBasedMap.BITS", "3");
    }

    @Test
    public void testSizeMethod() {
        ConcurrentArrayBasedMap<Integer> map = new ConcurrentArrayBasedMap<>(100000);

        assertEquals(0, map.size());

        for (int i = 0; i < 10; i++) {
            map.put(i);
        }

        assertEquals(10, map.size());

        for (int i = 0; i < 10; i++) {
            assertEquals((Integer) i, map.get(i));
        }

    }

    @Test
    public void testPutAndGetSingleChunk() {
        int items = ConcurrentArrayBasedMap.CHUNK_SIZE / 2;
        ConcurrentArrayBasedMap<Integer> map = new ConcurrentArrayBasedMap<>(100000);

        for (int i = 0; i < items; i++) {
            map.put(i);
        }

        assertEquals(items, map.size());

        for (int i = 0; i < items; i++) {
            assertEquals((Integer) i, map.get(i));
        }

        assertEquals(items, map.size());
    }

    @Test
    public void testPutAndGetMultipleChunks() {
        int items = ConcurrentArrayBasedMap.CHUNK_SIZE * 10;
        ConcurrentArrayBasedMap<Integer> map = new ConcurrentArrayBasedMap<>(100000);

        for (int i = 0; i < items; i++) {
            map.put(i);
        }

        assertEquals(items, map.size());

        for (int i = 0; i < items; i++) {
            assertEquals((Integer) i, map.get(i));
        }

        assertEquals(items, map.size());
    }
}