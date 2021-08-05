package com.ulyp.ui;

import com.ulyp.core.*;
import com.ulyp.core.impl.FileBasedCallRecordDatabase;
import com.ulyp.core.impl.RocksdbIndex;
import com.ulyp.core.printers.ObjectBinaryPrinter;
import com.ulyp.core.printers.ObjectBinaryPrinterType;
import com.ulyp.core.util.SingleTypeReflectionBasedResolver;
import com.ulyp.transport.RecordingInfo;
import com.ulyp.transport.TClassDescription;
import lombok.Value;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

public class ByThreadIdAggregationStrategy implements AggregationStrategy {

    private final AtomicLong idGen = new AtomicLong(0L);

    @Override
    public CallRecordTreeTabId getId(CallRecordTreeChunk chunk) {
        return new Key(chunk.getRecordingInfo().getThreadId(), chunk.getRecordingInfo().getThreadName());
    }

    @Override
    public CallRecordDatabase buildDatabase(MethodInfoDatabase methodInfoDatabase, TypeInfoDatabase typeInfoDatabase) {

        TypeResolver typeResolver = new SingleTypeReflectionBasedResolver(Integer.MAX_VALUE, Thread.class);
        typeInfoDatabase.addAll(
                Collections.singletonList(TClassDescription.newBuilder().setId(Integer.MAX_VALUE).setName(Thread.class.getName()).build())
        );

        MethodInfoList methodInfos = new MethodInfoList();
        Method threadRunMethod = Method.builder()
                .id(Integer.MAX_VALUE)
                .name("run")
                .returnsSomething(false)
                .isStatic(false)
                .isConstructor(false)
                .declaringType(typeResolver.get(Thread.class))
                .build();

        methodInfos.add(threadRunMethod);
        methodInfoDatabase.addAll(methodInfos);

        CallRecordDatabase database = new FileBasedCallRecordDatabase("" + idGen.incrementAndGet(), methodInfoDatabase, typeInfoDatabase, new RocksdbIndex());

        CallEnterRecordList enterRecords = new CallEnterRecordList();
        CallExitRecordList exitRecords = new CallExitRecordList();

        enterRecords.add(
                0,
                Integer.MAX_VALUE,
                typeResolver,
                new ObjectBinaryPrinter[] {ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance()},
                Thread.currentThread(),
                new Object[]{}
        );

        database.persistBatch(enterRecords, exitRecords);

        return database;
    }

    @Value
    private static class Key implements CallRecordTreeTabId {

        // Aggregate by <thread id, thread name> because same id (as well as name) might be reused by same JVM
        long threadId;
        String threadName;
    }
}
