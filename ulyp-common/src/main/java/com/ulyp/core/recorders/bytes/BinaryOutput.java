package com.ulyp.core.recorders.bytes;

import com.ulyp.core.TypeResolver;
import org.agrona.DirectBuffer;

public interface BinaryOutput extends AutoCloseable {

    int recursionDepth();

    int size();

    BinaryOutput nest();

    Checkpoint checkpoint();

    void write(boolean value);

    void write(int value);

    void write(long value);

    void write(byte c);

    void write(DirectBuffer buffer);

    void write(byte[] bytes);

    void write(String value);

    void write(Object object, TypeResolver typeResolver) throws Exception;

    void write(char val);

    void close() throws RuntimeException;
}
