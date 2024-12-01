package com.ulyp.core;

import com.ulyp.core.recorders.ObjectRecord;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class RecordedExitMethodCall extends RecordedMethodCall {

    private final long callId;
    private final ObjectRecord returnValue;
    private final boolean thrown;
}
