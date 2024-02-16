package com.ulyp.core;

import com.ulyp.core.recorders.ObjectRecord;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public class RecordedEnterMethodCall extends RecordedMethodCall {

    private final int methodId;
    private final ObjectRecord callee;
    private final List<ObjectRecord> arguments;
}
