package com.ulyp.core;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RecordedObject {

    private final byte recorderId;
    private final long typeId;
    private final byte[] value;
}
