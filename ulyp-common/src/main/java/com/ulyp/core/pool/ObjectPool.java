package com.ulyp.core.pool;

public interface ObjectPool<T extends PooledObject> {

    T borrow();

    void requite(T object);
}
