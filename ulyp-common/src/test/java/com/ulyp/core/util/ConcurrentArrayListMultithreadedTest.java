package com.ulyp.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrentArrayListMultithreadedTest {

    static {
        System.setProperty("ConcurrentArrayList.BITS", "3");
    }

    @Test
    void testPutAndGetSingleChunk() throws InterruptedException {
        int threads = 4;
        int putsPerThread = 1_000_000;
        ConcurrentArrayList<Integer> list = new ConcurrentArrayList<>(
                (threads + 1) * putsPerThread / 8
        );
        CountDownLatch countDownLatch = new CountDownLatch(threads + 1);
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        List<Future<int[]>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            int threadOffset = i;
            futures.add(executorService.submit(
                    () -> {
                        int[] keys = new int[putsPerThread];

                        countDownLatch.countDown();
                        try {
                            countDownLatch.await();
                        } catch (InterruptedException e) {
                            // NOP
                        }

                        for (int j = 0; j < putsPerThread; j++) {
                            int value = j * (threads + 1) + threadOffset;
                            keys[j] = list.add(value);
                        }

                        return keys;
                    }
            ));
        }

        countDownLatch.countDown();

        for (int threadOffset = 0; threadOffset < futures.size(); threadOffset++) {
            try {
                Future<int[]> future = futures.get(threadOffset);
                int[] keys = future.get();

                for (int j = 0; j < keys.length; j++) {
                    int actualValue = list.get(keys[j]);
                    int expectedValue = j * (threads + 1) + threadOffset;
                    assertEquals(expectedValue, actualValue);
                }
            } catch (Exception e) {
                Assertions.fail("Test failed: " + e.getMessage());
            }
        }

        assertEquals(putsPerThread * threads, list.size());

        executorService.shutdownNow();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }
}