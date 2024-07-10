package com.ulyp.core.bytes;

import com.ulyp.core.TypeResolver;
import lombok.SneakyThrows;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.jetbrains.annotations.TestOnly;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Common interface for byte containers. This is pretty similar to {@link java.io.OutputStream}, but provides additional
 * methods for writing objects with help of recorders, primitive data, arrays etc. It also allows nesting one output into another with recursion
 * depth tracking (it actually uses same output under the hood) and write position rollback
 */
public interface BytesOut extends AutoCloseable {

    int recursionDepth();

    BytesOut nest();

    Mark mark();

    int position();

    int bytesWritten(int prevOffset);

    void writeAt(int offset, int value);

    void write(boolean value);

    void write(int value);

    void writeVarInt(int v);

    void write(long value);

    void write(byte c);

    void write(DirectBuffer buffer);

    void write(byte[] bytes);

    void write(String value);

    void write(Object object, TypeResolver typeResolver) throws Exception;

    void write(char val);

    /**
     * Closing is only used for decrementing recursion depth, use {@link BytesOut#dispose()} to free the memory
     */
    DirectBuffer copy();

    void close() throws RuntimeException;

    int writeTo(BytesOutputSink sink) throws IOException;

    @SneakyThrows
    @TestOnly
    default BytesIn flip() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(output);
        writeTo(bufferedOutputStream);
        return new DirectBytesIn(new UnsafeBuffer(output.toByteArray()));
    }
}
