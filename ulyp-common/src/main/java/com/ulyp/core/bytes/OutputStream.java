package com.ulyp.core.bytes;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;

/**
 * Simple output stream which allows writing off-heap buffers. Not thread-safe
 */
@NotThreadSafe
public interface OutputStream extends BytesOutputSink {

    void flush() throws IOException;

    void write(byte b) throws IOException;

    void close() throws IOException;
}
