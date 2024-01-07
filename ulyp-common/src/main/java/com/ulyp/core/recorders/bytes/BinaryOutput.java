package com.ulyp.core.recorders.bytes;

import com.ulyp.core.TypeResolver;

// TODO remove duplicate methods
public interface BinaryOutput extends AutoCloseable {

    int recursionDepth();

    BinaryOutput nest();

    Checkpoint checkpoint();

    void append(boolean value);

    void append(int value);

    void append(long value);

    void append(byte c);

    void append(byte[] bytes);

    void append(String value);

    void append(Object object, TypeResolver typeResolver) throws Exception;

    void writeBool(boolean val);

    void writeChar(char val);

    void writeInt(int val);

    void writeLong(long val);

    void writeBytes(byte[] bytes);

    void writeString(final String value);

    void close() throws RuntimeException;
}
