package com.ulyp.core.pool;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import com.ulyp.core.metrics.Counter;
import com.ulyp.core.metrics.Metrics;

public class QueueBasedObjectPool<T extends PooledObject> implements ObjectPool<T> {

    private final Counter borrowCount;
    private final Counter allocateCount;
    private final Counter requiteCount;
    private final Supplier<T> allocator;
    private final Queue<T> queue = new ConcurrentLinkedQueue<>();
    private final AtomicLong borrowed = new AtomicLong(0L);
    private final AtomicLong requited = new AtomicLong(0L);

    public QueueBasedObjectPool(String name, int poolSize, Supplier<T> allocator, Metrics metrics) {
        this.borrowCount = metrics.getOrCreateCounter("ulyp.pool." + name + ".borrows");
        this.allocateCount = metrics.getOrCreateCounter("ulyp.pool." + name + ".allocates");
        this.requiteCount = metrics.getOrCreateCounter("ulyp.pool." + name + ".requites");
        this.allocator = allocator;
        // TODO make growable
        for (int i = 0; i < poolSize; i++) {
            queue.add(allocator.get());
        }
    }

    @Override
    public T borrow() {
        T borrowed = queue.poll();
        if (borrowed != null) {
            this.borrowed.incrementAndGet();
            borrowCount.inc();
            return borrowed;
        } else {
            allocateCount.inc();
            return allocator.get();
        }
    }

    @Override
    public void requite(T object) {
        if (borrowed.get() > requited.get()) {
            queue.add(object);
            requited.incrementAndGet();
            requiteCount.inc();
        }
    }
}
