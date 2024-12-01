package com.ulyp.core.bytes;

import com.ulyp.core.Resettable;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public interface Mark extends AutoCloseable, Resettable {

    default void reset() {

    }

    int writtenBytes();

    void rollback();

    void close() throws RuntimeException;
}
