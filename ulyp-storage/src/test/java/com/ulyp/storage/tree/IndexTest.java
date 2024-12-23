package com.ulyp.storage.tree;

import org.agrona.collections.LongArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

abstract class IndexTest {

    protected abstract Index buildIndex() throws IOException;

    @Test
    void testSingleWriteRead() {
        try (Index index = buildIndex()) {

            Assertions.assertNull(index.get(5));

            LongArrayList childrenCallIds = new LongArrayList();
            childrenCallIds.add(5L);
            childrenCallIds.add(2L);
            childrenCallIds.add(3L);

            CallRecordIndexState value = CallRecordIndexState.builder()
                    .id(5)
                    .enterMethodCallAddress(60)
                    .exitMethodCallAddr(72)
                    .subtreeSize(1)
                    .childrenCallIds(childrenCallIds)
                    .build();
            index.store(5, value);

            CallRecordIndexState valueFromIndex = index.get(5);

            Assertions.assertEquals(value, valueFromIndex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testMultipleWrites() {
        int count = 10000;

        try (Index index = buildIndex()) {

            Map<Long, CallRecordIndexState> map = new HashMap<>();

            for (int i = 0; i < count; i++) {
                CallRecordIndexState value = CallRecordIndexState.builder()
                        .id(i)
                        .enterMethodCallAddress(ThreadLocalRandom.current().nextLong())
                        .exitMethodCallAddr(ThreadLocalRandom.current().nextLong())
                        .subtreeSize(ThreadLocalRandom.current().nextInt(50))
                        .childrenCallIds(generateRandomLongList())
                        .build();
                map.put(Long.valueOf(i), value);
                index.store(i, value);
            }

            for (int iter = 0; iter < 5; iter++) {
                for (CallRecordIndexState valueToCheck : map.values()) {
                    Assertions.assertEquals(valueToCheck, index.get(valueToCheck.getId()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private LongArrayList generateRandomLongList() {
        int cnt = ThreadLocalRandom.current().nextInt(5);
        LongArrayList result = new LongArrayList();
        for (int i = 0; i < cnt; i++) {
            result.add(ThreadLocalRandom.current().nextLong());
        }
        return result;
    }
}