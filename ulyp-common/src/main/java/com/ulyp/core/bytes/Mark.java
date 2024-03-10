package com.ulyp.core.bytes;

import com.ulyp.core.Resettable;

public interface Mark extends AutoCloseable, Resettable {

    default void reset() {

    }

    int writtenBytes();

    void rollback();

    void close() throws RuntimeException;
}
