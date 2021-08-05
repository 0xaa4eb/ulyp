package com.ulyp.ui;

import com.ulyp.core.CallRecordDatabase;
import com.ulyp.core.MethodInfoDatabase;
import com.ulyp.core.TypeInfoDatabase;
import com.ulyp.core.impl.FileBasedCallRecordDatabase;
import com.ulyp.core.impl.RocksdbIndex;
import lombok.Value;

import java.util.concurrent.atomic.AtomicLong;

public class ByRecordingIdAggregationStrategy implements AggregationStrategy {

    private final AtomicLong idGen = new AtomicLong(0L);

    @Override
    public CallRecordTreeTabId getId(CallRecordTreeChunk chunk) {
        return new Key(chunk.getRecordingId());
    }

    @Override
    public CallRecordDatabase buildDatabase(MethodInfoDatabase methodInfoDatabase, TypeInfoDatabase typeInfoDatabase) {
        return new FileBasedCallRecordDatabase("" + idGen.incrementAndGet(), methodInfoDatabase, typeInfoDatabase, new RocksdbIndex());
    }

    @Value
    private static class Key implements CallRecordTreeTabId {

        long recordingId;
    }
}
