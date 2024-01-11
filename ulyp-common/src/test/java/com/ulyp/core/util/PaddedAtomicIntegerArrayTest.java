package com.ulyp.core.util;

import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class PaddedAtomicIntegerArrayTest {

    @Test
    public void testIndex() {
        PaddedAtomicIntegerArray array = new PaddedAtomicIntegerArray(64);

        for (int index = 0; index < 64; index++) {
            int value = ThreadLocalRandom.current().nextInt();
            array.set(index, value);
            assertEquals(value, array.get(index));
        }
    }
}