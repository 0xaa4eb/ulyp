package com.ulyp.core.bytes;

import org.agrona.DirectBuffer;

import java.io.IOException;

public interface BinaryOutputSink {

    void write(DirectBuffer buffer, int length) throws IOException;
}
