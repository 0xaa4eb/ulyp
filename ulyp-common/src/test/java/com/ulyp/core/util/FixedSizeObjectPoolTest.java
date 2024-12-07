package com.ulyp.core.util;

import com.ulyp.core.Resettable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

class FixedSizeObjectPoolTest {

    private static final AtomicInteger idGen = new AtomicInteger(-1);

    class Entry implements Resettable {

        private final int id;

        Entry() {
            this.id = idGen.incrementAndGet();
        }

        @Override
        public void reset() {

        }
    }

    @Test
    void testReuseSameItem() {
        FixedSizeObjectPool<Entry> pool = new FixedSizeObjectPool<>(
                Entry::new,
                3
        );

        Assertions.assertEquals(0, pool.size());

        Entry borrow1 = pool.borrow();
        pool.requite(borrow1);

        Assertions.assertEquals(1, pool.size());

        borrow1 = pool.borrow();
        pool.requite(borrow1);

        Assertions.assertEquals(1, pool.size());
    }

    @Test
    void testPool() {
        FixedSizeObjectPool<Entry> pool = new FixedSizeObjectPool<>(
                Entry::new,
                2
        );

        Assertions.assertEquals(0, pool.size());

        Entry borrow1 = pool.borrow();
        Entry borrow2 = pool.borrow();
        Entry borrow3 = pool.borrow();

        pool.requite(borrow1);
        pool.requite(borrow2);
        pool.requite(borrow3);

        // the last one is discarded
        Assertions.assertEquals(2, pool.size());
    }
}