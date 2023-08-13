package com.ulyp.storage;

import java.io.File;
import java.util.function.Function;
import java.util.function.Supplier;

import com.ulyp.core.repository.Repository;
import com.ulyp.storage.impl.RecordedCallState;
import com.ulyp.storage.impl.RocksdbIndex;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ReaderSettings {

    private final File file;
    private final boolean ignoreCallIdInconsistency;
    private final boolean autoStartReading;
    @Builder.Default
    private final Filter filter = recording -> true;
    @Builder.Default
    private final Supplier<Index> indexSupplier;
}
