package com.ulyp.core.bytes;

import org.agrona.DirectBuffer;

import java.io.IOException;

public interface BytesOutputSink {

    void write(DirectBuffer buffer, int length) throws IOException;
}
