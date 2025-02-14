package com.ulyp.core.util;

import com.ulyp.core.Resettable;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FixedSizeObjectPoolTest {

    private static class TestObject implements Resettable {
        private int value;
        private boolean wasReset = false;

        public TestObject(int value) {
            this.value = value;
        }

        @Override
        public void reset() {
            wasReset = true;
            value = 0;
        }

        public int getValue() {
            return value;
        }

        public boolean wasReset() {
            return wasReset;
        }
    }

    @Test
    void shouldCreateNewObjectWhenPoolIsEmpty() {
        FixedSizeObjectPool<TestObject> pool = new FixedSizeObjectPool<>(() -> new TestObject(42), 2);
        
        TestObject borrowed = pool.borrow();
        
        assertNotNull(borrowed, "Borrowed object should not be null");
        assertEquals(42, borrowed.getValue(), "Newly created object should have initial value");
    }

    @Test
    void shouldNotExceedCapacity() {
        int capacity = 2;
        FixedSizeObjectPool<TestObject> pool = new FixedSizeObjectPool<>(() -> new TestObject(42), capacity);
        
        TestObject obj1 = new TestObject(1);
        TestObject obj2 = new TestObject(2);
        TestObject obj3 = new TestObject(3);
        
        pool.requite(obj1);
        pool.requite(obj2);
        pool.requite(obj3); // This should be ignored as pool is full
        
        assertEquals(capacity, pool.size(), 
            "Pool size should not exceed capacity even when more objects are requited");
    }

    @Test
    void shouldCreateNewObjectWhenPoolIsFull() {
        FixedSizeObjectPool<TestObject> pool = new FixedSizeObjectPool<>(() -> new TestObject(42), 1);
        
        TestObject first = pool.borrow();
        TestObject second = pool.borrow();
        
        assertNotNull(second, "Second borrowed object should not be null");
        assertNotSame(first, second, "Should create new object when pool is empty");
    }

    @Test
    void shouldMaintainCorrectSize() {
        FixedSizeObjectPool<TestObject> pool = new FixedSizeObjectPool<>(() -> new TestObject(42), 3);
        
        assertEquals(0, pool.size(), "Initial pool size should be 0");
        
        TestObject obj = pool.borrow();
        assertEquals(0, pool.size(), "Pool size should be 0 after borrowing from empty pool");
        
        pool.requite(obj);
        assertEquals(1, pool.size(), "Pool size should be 1 after requiting object");
        
        pool.borrow();
        assertEquals(0, pool.size(), "Pool size should be 0 after borrowing requited object");
    }

    @Test
    void shouldHandleZeroCapacity() {
        FixedSizeObjectPool<TestObject> pool = new FixedSizeObjectPool<>(() -> new TestObject(42), 0);
        
        TestObject obj = pool.borrow();
        assertNotNull(obj, "Should create new object even with zero capacity");
        
        pool.requite(obj);
        assertEquals(0, pool.size(), "Pool size should remain 0 for zero capacity pool");
    }
}