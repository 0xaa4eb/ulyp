package com.ulyp.storage.reader;

import com.ulyp.core.mem.InputBinaryList;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BinaryListWithAddress {

    long address;
    InputBinaryList bytes;
}
