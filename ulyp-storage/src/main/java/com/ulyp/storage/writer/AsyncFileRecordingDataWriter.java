package com.ulyp.storage.writer;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.SerializedMethodList;
import com.ulyp.core.mem.SerializedRecordedMethodCallList;
import com.ulyp.core.mem.SerializedTypeList;
import com.ulyp.core.util.NamedThreadFactory;
import com.ulyp.storage.StorageException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.*;

@Slf4j
public class AsyncFileRecordingDataWriter implements RecordingDataWriter {

    private final RecordingDataWriter delegate;
    private final ExecutorService executorService;
    private volatile Future<?> future;

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
    public synchronized void sync(Duration duration) throws InterruptedException, TimeoutException {
        if (future != null) {
            try {
                future.get(duration.toMillis(), TimeUnit.MILLISECONDS);
            } catch (ExecutionException e) {
                throw new RuntimeException("Sync failed because there was error writing to disk", e);
            }
            future = null;
        }
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
    public void write(SerializedTypeList types) {
        writeAsync(() -> delegate.write(types));
    }

    @Override
    public void write(SerializedRecordedMethodCallList callRecords) {
        writeAsync(() -> delegate.write(callRecords));
    }

    @Override
    public long estimateBytesWritten() {
        return delegate.estimateBytesWritten();
    }

    @Override
    public void write(SerializedMethodList methods) {
        writeAsync(() -> delegate.write(methods));
    }

    private void writeAsync(Runnable runnable) {
        try {
            future = executorService.submit(runnable);
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
