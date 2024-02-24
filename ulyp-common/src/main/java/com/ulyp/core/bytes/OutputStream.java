package com.ulyp.core.bytes;

import java.io.IOException;

/**
 * Simple output stream which allows writing off-heap buffers. Not thread-safe
 */
public interface OutputStream extends BinaryOutputSink {

    void flush() throws IOException;

    void write(byte b) throws IOException;

    void close() throws IOException;
}
