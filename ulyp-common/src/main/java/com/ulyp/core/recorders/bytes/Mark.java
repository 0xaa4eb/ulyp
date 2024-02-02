package com.ulyp.core.recorders.bytes;

public interface Mark extends AutoCloseable {

    int writtenBytes();

    void rollback();

    void close() throws RuntimeException;
}
