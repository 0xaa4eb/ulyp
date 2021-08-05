package com.ulyp.core;

import com.ulyp.transport.BinaryTypeDecoder;
import com.ulyp.transport.BinaryTypeEncoder;

public class TypeList extends AbstractBinaryEncodedList<BinaryTypeEncoder, BinaryTypeDecoder> {

    public void add(Type type) {
        super.add(type::serialize);
    }
}
