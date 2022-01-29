package com.ulyp.storage.impl;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.storage.StorageWriter;
import com.ulyp.storage.util.NamedThreadFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AsyncFileStorageWriter implements StorageWriter {

    private final StorageWriter delegate;
    private final ExecutorService executorService;

    public AsyncFileStorageWriter(StorageWriter delegate) {
        this.delegate = delegate;
        this.executorService = Executors.newFixedThreadPool(
                1,
                new NamedThreadFactory("AsyncWriter-" + delegate.toString(), true)
        );
    }

    @Override
    public void write(ProcessMetadata processMetadata) {
        writeAsync(() -> delegate.write(processMetadata));
    }

    @Override
    public void write(RecordingMetadata recordingMetadata) {
        writeAsync(() -> delegate.write(recordingMetadata));
    }

    @Override
    public void write(TypeList types) {
        writeAsync(() -> delegate.write(types));
    }

    @Override
    public void write(RecordedMethodCallList callRecords) {
        writeAsync(() -> delegate.write(callRecords));
    }

    @Override
    public void write(MethodList methods) {
        writeAsync(() -> delegate.write(methods));
    }

    private void writeAsync(Runnable runnable) {
        try {
            executorService.submit(runnable);
        } catch (RejectedExecutionException ex) {
            log.error("Write rejected, some data may be missing!");
        }
    }

    @Override
    @SneakyThrows
    public synchronized void close() {
        try {
            executorService.shutdown();
            executorService.awaitTermination(30, TimeUnit.SECONDS);
        } finally {
            delegate.close();
        }
    }
}
