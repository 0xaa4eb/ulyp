package com.ulyp.core.bytes;

public interface Mark extends AutoCloseable {

    int writtenBytes();

    void rollback();

    void close() throws RuntimeException;
}
