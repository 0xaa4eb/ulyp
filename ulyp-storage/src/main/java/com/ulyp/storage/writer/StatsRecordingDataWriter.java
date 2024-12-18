package com.ulyp.storage.writer;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.SerializedMethodList;
import com.ulyp.core.mem.SerializedRecordedMethodCallList;
import com.ulyp.core.mem.SerializedTypeList;
import com.ulyp.core.metrics.BytesCounter;
import com.ulyp.core.metrics.Metrics;
import com.ulyp.storage.StorageException;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class StatsRecordingDataWriter implements RecordingDataWriter {

    private final RecordingDataWriter delegate;
    private final BytesCounter typeBytesWritten;
    private final BytesCounter methodBytesWritten;
    private final BytesCounter methodCallBytesWritten;
    private final AtomicLong totalBytesWritten = new AtomicLong(0L);

    public StatsRecordingDataWriter(Metrics metrics, RecordingDataWriter delegate) {
        this.delegate = delegate;
        this.typeBytesWritten = metrics.getOrCreateByteCounter("writer.bytes.types");
        this.methodBytesWritten = metrics.getOrCreateByteCounter("writer.bytes.methods");
        this.methodCallBytesWritten = metrics.getOrCreateByteCounter("writer.bytes.calls");
    }

    @Override
    public void reset(ResetRequest resetRequest) throws StorageException {
        delegate.reset(resetRequest);
    }

    @Override
    public void sync(Duration duration) throws InterruptedException, TimeoutException {
        delegate.sync(duration);
    }

    @Override
    public void write(ProcessMetadata processMetadata) throws StorageException {
        delegate.write(processMetadata);
    }

    @Override
    public void write(RecordingMetadata recordingMetadata) throws StorageException {
        delegate.write(recordingMetadata);
    }

    @Override
    public void write(SerializedTypeList types) throws StorageException {
        totalBytesWritten.addAndGet(types.byteLength());
        delegate.write(types);
        typeBytesWritten.add(types.byteLength(), types.size());
    }

    @Override
    public void write(SerializedRecordedMethodCallList callRecords) throws StorageException {
        totalBytesWritten.addAndGet(callRecords.bytesWritten());
        delegate.write(callRecords);
        methodCallBytesWritten.add(callRecords.bytesWritten(), callRecords.size());
    }

    @Override
    public long estimateBytesWritten() {
        return totalBytesWritten.get();
    }

    @Override
    public void write(SerializedMethodList methods) throws StorageException {
        totalBytesWritten.addAndGet(methods.byteLength());
        delegate.write(methods);
        methodBytesWritten.add(methods.byteLength(), methods.size());
    }

    @Override
    public void close() throws StorageException {
        delegate.close();
    }
}
