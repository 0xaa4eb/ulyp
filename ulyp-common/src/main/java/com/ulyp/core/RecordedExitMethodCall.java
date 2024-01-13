package com.ulyp.core;

import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.transport.BinaryRecordedExitMethodCallDecoder;
import com.ulyp.transport.BooleanType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class RecordedExitMethodCall extends RecordedMethodCall {

    private final ObjectRecord returnValue;
    private final boolean thrown;
}
