package com.ulyp.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Recorded method call. It's either an enter call i.e. when arguments are known or exit call when return value is known
 */
@SuperBuilder
@AllArgsConstructor
@Getter
public abstract class RecordedMethodCall {
    private final long methodId;
    private final long callId;
}
