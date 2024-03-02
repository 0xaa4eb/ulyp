package com.ulyp.storage.writer;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.SerializedMethodList;
import com.ulyp.core.mem.SerializedRecordedMethodCallList;
import com.ulyp.core.mem.SerializedTypeList;
import com.ulyp.core.metrics.BytesCounter;
import com.ulyp.core.metrics.Metrics;
import com.ulyp.storage.StorageException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatsRecordingDataWriter implements RecordingDataWriter {

    private final RecordingDataWriter delegate;
    private final BytesCounter typeBytes;
    private final BytesCounter methodBytes;
    private final BytesCounter callsBytes;

    private final PerTypeStats totalBytesWritten = new PerTypeStats("Total bytes written");

    public StatsRecordingDataWriter(Metrics metrics, RecordingDataWriter delegate) {
        this.delegate = delegate;
        this.typeBytes = metrics.getOrCreateByteCounter("writer.bytes.types");
        this.methodBytes = metrics.getOrCreateByteCounter("writer.bytes.methods");
        this.callsBytes = metrics.getOrCreateByteCounter("writer.bytes.calls");
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
        totalBytesWritten.addBytes(types.byteLength());
        delegate.write(types);
        typeBytes.add(types.byteLength(), types.size());
    }

    @Override
    public void write(SerializedRecordedMethodCallList callRecords) throws StorageException {
        totalBytesWritten.addBytes(callRecords.bytesWritten());
        delegate.write(callRecords);
        callsBytes.add(callRecords.bytesWritten(), callRecords.size());
    }

    @Override
    public long estimateBytesWritten() {
        return totalBytesWritten.getTotalBytes();
    }

    @Override
    public void write(SerializedMethodList methods) throws StorageException {
        totalBytesWritten.addBytes(methods.byteLength());
        delegate.write(methods);
        methodBytes.add(methods.byteLength(), methods.size());
    }

    @Override
    public void close() throws StorageException {
        delegate.close();
    }
}
