package com.ulyp.core;

import com.google.protobuf.ByteString;
import com.ulyp.transport.*;

// Flexible SBE wrapper
public class MethodList extends AbstractBinaryEncodedList<BinaryMethodEncoder, BinaryMethodDecoder> {

    public MethodList() {
    }

    public MethodList(ByteString bytes) {
        super(bytes);
    }

    public void add(Method method) {
        super.add(method::serialize);
    }
}
