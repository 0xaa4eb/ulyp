package com.ulyp.storage;

import java.io.File;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ReaderSettings {

    private final File file;
    private final boolean ignoreCallIdInconsistency;
    private final boolean autoStartReading;
}
