package com.ulyp.core.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class ConcurrentArrayListTest {

    static {
        System.setProperty("ConcurrentArrayList.BITS", "3");
    }

    @Test
    public void testSizeMethod() {
        ConcurrentArrayList<Integer> map = new ConcurrentArrayList<>(100000);

        assertEquals(0, map.size());

        for (int i = 0; i < 10; i++) {
            map.add(i);
        }

        assertEquals(10, map.size());

        for (int i = 0; i < 10; i++) {
            assertEquals((Integer) i, map.get(i));
        }

    }

    @Test
    public void testPutAndGetSingleChunk() {
        int items = ConcurrentArrayList.CHUNK_SIZE / 2;
        ConcurrentArrayList<Integer> map = new ConcurrentArrayList<>(100000);

        for (int i = 0; i < items; i++) {
            map.add(i);
        }

        assertEquals(items, map.size());

        for (int i = 0; i < items; i++) {
            assertEquals((Integer) i, map.get(i));
        }

        assertEquals(items, map.size());
    }

    @Test
    public void testPutAndGetMultipleChunks() {
        int items = ConcurrentArrayList.CHUNK_SIZE * 10;
        ConcurrentArrayList<Integer> map = new ConcurrentArrayList<>(100000);

        for (int i = 0; i < items; i++) {
            map.add(i);
        }

        assertEquals(items, map.size());

        for (int i = 0; i < items; i++) {
            assertEquals((Integer) i, map.get(i));
        }

        assertEquals(items, map.size());
    }
}