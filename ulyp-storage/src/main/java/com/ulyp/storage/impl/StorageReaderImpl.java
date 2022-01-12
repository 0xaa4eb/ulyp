package com.ulyp.storage.impl;

import com.ulyp.agent.transport.NamedThreadFactory;
import com.ulyp.core.Method;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.BinaryList;
import com.ulyp.core.util.LockGuard;
import com.ulyp.storage.Recording;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.StorageReader;
import com.ulyp.transport.BinaryMethodDecoder;
import com.ulyp.transport.BinaryRecordingMetadataDecoder;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StorageReaderImpl implements StorageReader {

    private final ExecutorService executorService;
    private final Lock lock = new ReentrantLock();

    private final List<Recording> recordings = new ArrayList<>();

    public StorageReaderImpl(File file) {
        executorService = Executors.newFixedThreadPool(
                1,
                new NamedThreadFactory("Reader-" + file.toString(), false)
        );
        try {
            Runnable task = new StorageReaderTask(file);
            executorService.submit(task);
        } catch (IOException e) {
            throw new StorageException("Could not start reader task for file " + file, e);
        }
    }

    private class StorageReaderTask implements Runnable, Closeable {

        private final BinaryListFileReader reader;

        private StorageReaderTask(File file) throws IOException {
            this.reader = new BinaryListFileReader(file);
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {

                try {
                    BinaryList data  = this.reader.read(Duration.ofSeconds(1));

                    if (data == null) {
                        continue;
                    }

                    switch(data.id()) {
                        case RecordingMetadata.WIRE_ID:
                            onRecordingMetadata(data);
                            break;
                        default:
                            throw new StorageException("Unknown binary data id " + data.id());
                    }
                } catch (Exception e) {

                    // TODO show in UI
                    e.printStackTrace();
                    return;
                }
            }
        }

        @Override
        public void close() throws IOException {

        }

        private void onRecordingMetadata(BinaryList data) {
            try (LockGuard guard = new LockGuard(lock)) {
                UnsafeBuffer buffer = new UnsafeBuffer();
                data.iterator().next().wrapValue(buffer);
                BinaryRecordingMetadataDecoder decoder = new BinaryRecordingMetadataDecoder();
                decoder.wrap(buffer, 0, BinaryRecordingMetadataDecoder.BLOCK_LENGTH, 0);
                RecordingMetadata metadata = RecordingMetadata.deserialize(decoder);
                recordings.add(Recording.builder().id(metadata.getId()).build());
            }
        }
    }

    @Override
    public List<Recording> availableRecordings() {
        try (LockGuard guard = new LockGuard(lock)) {
            return new ArrayList<>(recordings);
        }
    }

    @Override
    public void close() throws StorageException {
        executorService.shutdownNow();
        try {
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new StorageException("Interrupted", e);
        }
    }
}
