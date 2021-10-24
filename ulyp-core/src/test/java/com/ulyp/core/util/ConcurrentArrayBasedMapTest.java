package com.ulyp.core.util;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConcurrentArrayBasedMapTest {

    @Test
    public void testPutAndGetSingleChunk() {
        ConcurrentArrayBasedMap<Integer> map = new ConcurrentArrayBasedMap<>();

        for (int i = 0; i < 10; i++) {
            map.put(i);
        }

        for (int i = 0; i < 10; i++) {
            Assert.assertEquals((Integer) i, map.get(i));
        }
    }

    @Test
    public void testPutAndGetMultipleChunks() {
        ConcurrentArrayBasedMap<Integer> map = new ConcurrentArrayBasedMap<>();

        for (int i = 0; i < 100000; i++) {
            map.put(i);
        }

        for (int i = 0; i < 100000; i++) {
            Assert.assertEquals((Integer) i, map.get(i));
        }
    }
}