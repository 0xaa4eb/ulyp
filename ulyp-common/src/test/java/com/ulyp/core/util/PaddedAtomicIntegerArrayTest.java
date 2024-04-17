package com.ulyp.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

class PaddedAtomicIntegerArrayTest {

    @Test
    void testIndex() {
        PaddedAtomicIntegerArray array = new PaddedAtomicIntegerArray(64);

        for (int index = 0; index < 64; index++) {
            int value = ThreadLocalRandom.current().nextInt();
            array.set(index, value);
            Assertions.assertEquals(value, array.get(index));
        }
    }
}