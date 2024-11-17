package com.ulyp.core.bytes;

import org.agrona.DirectBuffer;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;

@NotThreadSafe
public interface BytesOutputSink {

    void write(DirectBuffer buffer, int length) throws IOException;
}
