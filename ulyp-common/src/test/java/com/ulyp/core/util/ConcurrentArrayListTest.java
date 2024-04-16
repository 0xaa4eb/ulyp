package com.ulyp.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConcurrentArrayListTest {

    static {
        System.setProperty("ConcurrentArrayList.BITS", "3");
    }

    @Test
    void testSizeMethod() {
        ConcurrentArrayList<Integer> map = new ConcurrentArrayList<>(100000);

        Assertions.assertEquals(0, map.size());

        for (int i = 0; i < 10; i++) {
            map.add(i);
        }

        Assertions.assertEquals(10, map.size());

        for (int i = 0; i < 10; i++) {
            Assertions.assertEquals((Integer) i, map.get(i));
        }

    }

    @Test
    void testPutAndGetSingleChunk() {
        int items = ConcurrentArrayList.CHUNK_SIZE / 2;
        ConcurrentArrayList<Integer> map = new ConcurrentArrayList<>(100000);

        for (int i = 0; i < items; i++) {
            map.add(i);
        }

        Assertions.assertEquals(items, map.size());

        for (int i = 0; i < items; i++) {
            Assertions.assertEquals((Integer) i, map.get(i));
        }

        Assertions.assertEquals(items, map.size());
    }

    @Test
    void testPutAndGetMultipleChunks() {
        int items = ConcurrentArrayList.CHUNK_SIZE * 10;
        ConcurrentArrayList<Integer> map = new ConcurrentArrayList<>(100000);

        for (int i = 0; i < items; i++) {
            map.add(i);
        }

        Assertions.assertEquals(items, map.size());

        for (int i = 0; i < items; i++) {
            Assertions.assertEquals((Integer) i, map.get(i));
        }

        Assertions.assertEquals(items, map.size());
    }
}