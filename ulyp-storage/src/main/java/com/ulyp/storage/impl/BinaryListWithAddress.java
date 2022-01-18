package com.ulyp.storage.impl;

import com.ulyp.core.mem.BinaryList;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BinaryListWithAddress {

    long address;
    BinaryList bytes;
}
