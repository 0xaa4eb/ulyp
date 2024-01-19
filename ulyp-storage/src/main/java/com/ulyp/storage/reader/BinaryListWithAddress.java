package com.ulyp.storage.reader;

import com.ulyp.core.mem.BinaryList;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BinaryListWithAddress {

    long address;
    BinaryList.In bytes;
}
