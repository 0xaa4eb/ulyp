package com.ulyp.storage.writer;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.storage.StorageException;
import com.ulyp.core.util.NamedThreadFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AsyncFileRecordingDataWriter implements RecordingDataWriter {

    private final RecordingDataWriter delegate;
    private final ExecutorService executorService;

    public AsyncFileRecordingDataWriter(RecordingDataWriter delegate) {
        this.delegate = delegate;
        this.executorService = Executors.newFixedThreadPool(
                1,
                NamedThreadFactory.builder()
                        .name("AsyncWriter-" + delegate.toString())
                        .daemon(true)
                        .build()
        );
    }

    @Override
    public void reset(ResetRequest resetRequest) throws StorageException {
        writeAsync(() -> delegate.reset(resetRequest));
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
            executorService.shutdownNow();
            executorService.awaitTermination(30, TimeUnit.SECONDS);
        } finally {
            delegate.close();
        }
    }
}
