package com.ulyp.core;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RecordedObject {

    private final byte printerId;
    private final long typeId;
    private final byte[] value;
}
