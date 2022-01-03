package com.ulyp.database.flat;

import com.ulyp.core.mem.BinaryList;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BinaryListWithId {

    private final BinaryList binaryList;
    private final long id;
}
