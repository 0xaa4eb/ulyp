package com.ulyp.storage.writer;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.storage.StorageException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class StatsRecordingDataWriter implements RecordingDataWriter {

    private final RecordingDataWriter delegate;

    private final PerTypeStats typeStats = new PerTypeStats("Type");
    private final PerTypeStats methodStats = new PerTypeStats("Method");
    private final PerTypeStats callStats = new PerTypeStats("Recorded call");
    private final PerTypeStats callBufferStats = new PerTypeStats("Recorded call buffers");

    @Override
    public void reset(ResetRequest resetRequest) throws StorageException {
        typeStats.reset();
        methodStats.reset();
        callStats.reset();
        callBufferStats.reset();
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
    public void write(TypeList types) throws StorageException {
        delegate.write(types);
//        typeStats.addToCount(types.size()); // TODO unlock RecorderCurrentSessionCountTest
//        typeStats.addBytes(types.byteLength());
    }

    @Override
    public void write(RecordedMethodCallList callRecords) throws StorageException {
        delegate.write(callRecords);
        // Every call is recorded twice: as enter method call and exit method calls, therefore the value needs to be adjusted
//        callStats.addToCount(callRecords.size() / 2);
//        callStats.addBytes(callRecords.byteLength());
        // TODO
//        callBufferStats.addToCount(1);
    }

    @Override
    public void write(MethodList methods) throws StorageException {
        delegate.write(methods);
//        methodStats.addToCount(methods.size());
//        methodStats.addBytes(methods.byteLength());
    }

    public PerTypeStats getCallStats() {
        return callStats;
    }

    @Override
    public void close() throws StorageException {
        log.info("File stats: {}", typeStats);
        log.info("File stats: {}", methodStats);
        log.info("File stats: {}", callStats);
        log.info("File stats: {}", callBufferStats);
        delegate.close();
    }
}
