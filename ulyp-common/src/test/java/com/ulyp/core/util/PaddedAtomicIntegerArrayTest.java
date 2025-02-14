package com.ulyp.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PaddedAtomicIntegerArrayTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 8, 16, 32, 64, 128})
    void shouldCreateArrayWithValidPowerOfTwoCapacity(int capacity) {
        assertDoesNotThrow(
            () -> new PaddedAtomicIntegerArray(capacity),
            "Should create array with valid power of two capacity: " + capacity
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 3, 5, 7, 9, 15, 100})
    void shouldThrowExceptionForNonPowerOfTwoCapacity(int capacity) {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new PaddedAtomicIntegerArray(capacity),
            "Should throw IllegalArgumentException for non-power of two capacity: " + capacity
        );
        assertTrue(exception.getMessage().contains("Expected threads must be power of two"),
            "Exception message should mention power of two requirement");
    }

    @Test
    void shouldGetAndSetValues() {
        PaddedAtomicIntegerArray array = new PaddedAtomicIntegerArray(4);
        
        array.set(0, 42);
        array.set(1, 100);
        array.set(2, -1);
        array.set(3, Integer.MAX_VALUE);

        assertEquals(42, array.get(0), "Value at index 0 should be 42");
        assertEquals(100, array.get(1), "Value at index 1 should be 100");
        assertEquals(-1, array.get(2), "Value at index 2 should be -1");
        assertEquals(Integer.MAX_VALUE, array.get(3), "Value at index 3 should be Integer.MAX_VALUE");
    }

    @Test
    void shouldCompareAndSetSuccessfully() {
        PaddedAtomicIntegerArray array = new PaddedAtomicIntegerArray(2);
        
        array.set(0, 10);
        
        assertTrue(array.compareAndSet(0, 10, 20),
            "CompareAndSet should return true when expected value matches");
        assertEquals(20, array.get(0),
            "Value should be updated after successful compareAndSet");
    }

    @Test
    void shouldFailCompareAndSetWhenValuesDontMatch() {
        PaddedAtomicIntegerArray array = new PaddedAtomicIntegerArray(2);
        
        array.set(0, 10);
        
        assertFalse(array.compareAndSet(0, 15, 20),
            "CompareAndSet should return false when expected value doesn't match");
        assertEquals(10, array.get(0),
            "Value should remain unchanged after failed compareAndSet");
    }

    @Test
    void shouldMaintainThreadSafety() throws InterruptedException {
        PaddedAtomicIntegerArray array = new PaddedAtomicIntegerArray(2);
        int numThreads = 4;
        int incrementsPerThread = 1000;
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    int current;
                    do {
                        current = array.get(0);
                    } while (!array.compareAndSet(0, current, current + 1));
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(numThreads * incrementsPerThread, array.get(0),
            "Final value should equal the total number of increments across all threads");
    }
} 