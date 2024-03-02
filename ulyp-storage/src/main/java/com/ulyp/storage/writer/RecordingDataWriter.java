package com.ulyp.storage.writer;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.SerializedMethodList;
import com.ulyp.core.mem.SerializedRecordedMethodCallList;
import com.ulyp.core.mem.SerializedTypeList;
import com.ulyp.core.metrics.Metrics;
import com.ulyp.storage.StorageException;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

public interface RecordingDataWriter extends AutoCloseable {

    static RecordingDataWriter statsRecording(Metrics metrics, RecordingDataWriter delegate) {
        return new StatsRecordingDataWriter(metrics, delegate);
    }

    static RecordingDataWriter async(RecordingDataWriter delegate) {
        return new AsyncFileRecordingDataWriter(delegate);
    }

    static RecordingDataWriter forFile(File file) {
        return new FileRecordingDataWriter(file);
    }

    static RecordingDataWriter blackhole() {
        return new BlackholeRecordingDataWriter();
    }

    /**
     * Waits until all pending data is flushed to page cache
     */
    void sync(Duration duration) throws InterruptedException, TimeoutException;

    void write(ProcessMetadata processMetadata) throws StorageException;

    /**
     * Trims a recording file. The request should have all known type and method metadata.
     */
    void reset(ResetRequest resetRequest) throws StorageException;

    void write(RecordingMetadata recordingMetadata) throws StorageException;

    void write(SerializedTypeList types) throws StorageException;

    void write(SerializedMethodList methods) throws StorageException;

    void write(SerializedRecordedMethodCallList callRecords) throws StorageException;

    long estimateBytesWritten();

    void close() throws StorageException;
}
