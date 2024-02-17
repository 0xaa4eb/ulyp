package com.ulyp.core.pool;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;

import com.ulyp.core.metrics.NullMetrics;

import lombok.Getter;

public class SimpleSmallObjectPoolTest {

    private static final AtomicLong ctr = new AtomicLong(0);

    class TestPoolObject extends PooledObject {

        @Getter
        private final long id;

        public TestPoolObject() {
            id = ctr.incrementAndGet();
        }
    }

    @Test
    public void testObjectReuse() {
        QueueBasedObjectPool<TestPoolObject> pool = new QueueBasedObjectPool<>("test", 10000, TestPoolObject::new, new NullMetrics());
        Set<Long> usedObjects = new HashSet<>();

        for (int i = 0; i < 500000; i++) {
            TestPoolObject borrow = pool.borrow();
            pool.requite(borrow);
            usedObjects.add(borrow.getId());
        }

        Assert.assertTrue(usedObjects.size() > 49000);
        Assert.assertTrue(usedObjects.size() < 51000);
    }
}