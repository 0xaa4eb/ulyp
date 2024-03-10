package com.ulyp.core.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.ulyp.core.Resettable;

import static org.junit.Assert.*;

public class FixedSizeObjectPoolTest {

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
    public void testReuseSameItem() {
        FixedSizeObjectPool<Entry> pool = new FixedSizeObjectPool<>(
                Entry::new,
                3
        );

        Assert.assertEquals(0, pool.size());

        Entry borrow1 = pool.borrow();
        pool.requite(borrow1);

        Assert.assertEquals(1, pool.size());

        borrow1 = pool.borrow();
        pool.requite(borrow1);

        Assert.assertEquals(1, pool.size());
    }

    @Test
    public void testPool() {
        FixedSizeObjectPool<Entry> pool = new FixedSizeObjectPool<>(
                Entry::new,
                2
        );

        Assert.assertEquals(0, pool.size());

        Entry borrow1 = pool.borrow();
        Entry borrow2 = pool.borrow();
        Entry borrow3 = pool.borrow();

        pool.requite(borrow1);
        pool.requite(borrow2);
        pool.requite(borrow3);

        // the last one is discarded
        Assert.assertEquals(2, pool.size());
    }
}