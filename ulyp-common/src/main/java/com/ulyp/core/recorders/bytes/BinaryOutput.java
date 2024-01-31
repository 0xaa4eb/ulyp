package com.ulyp.core.recorders.bytes;

import com.ulyp.core.TypeResolver;

import java.io.IOException;
import java.io.OutputStream;

public interface BinaryOutput extends AutoCloseable {

    int recursionDepth();

    BinaryOutput nest();

    Checkpoint checkpoint();

    int currentOffset();

    void writeAt(int offset, int value);

    void write(boolean value);

    void write(int value);

    void write(long value);

    void write(byte c);

/*    void write(byte[] bytes);*/

    void write(String value);

    void write(Object object, TypeResolver typeResolver) throws Exception;

    void write(char val);

    /**
     * Returns borrowed memory to the pool for further use
     */
    void dispose();

    /**
     * Closing is only used for decrementing recursion depth, use {@link BinaryOutput#dispose()} to free the memory
     */
    void close() throws RuntimeException;

    int writeTo(OutputStream outputStream) throws IOException;
}
