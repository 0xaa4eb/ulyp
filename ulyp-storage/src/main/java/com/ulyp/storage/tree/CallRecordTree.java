package com.ulyp.storage.tree;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.ulyp.core.Method;
import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.Type;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.repository.InMemoryRepository;
import com.ulyp.core.repository.Repository;
import com.ulyp.core.util.Backoff;
import com.ulyp.core.util.FixedDelayBackoff;
import com.ulyp.storage.reader.RecordingDataReader;
import com.ulyp.storage.reader.RecordingDataReaderJob;
import com.ulyp.storage.StorageException;

import lombok.Getter;

/**
 * Primary call tree implementation which is used by tests and UI (thus is located here)
 */
public class CallRecordTree implements AutoCloseable {

    private final RecordingDataReader dataReader;
    private final boolean readContinuously;
    @Getter
    private final CompletableFuture<Void> completeFuture;
    private final InMemoryRepository<Integer, Type> types = new InMemoryRepository<>();
    private final Repository<Integer, Method> methods = new InMemoryRepository<>();
    private final InMemoryRepository<Integer, RecordingState> recordings = new InMemoryRepository<>();
    private final Index index;
    private volatile RecordingListener recordingListener;
    private final Lock listenerLock = new ReentrantLock();

    CallRecordTree(RecordingDataReader dataReader,
                   RecordingListener recordingListener,
                   Supplier<Index> indexSupplier,
                   boolean readContinuously) {
        this.recordingListener = recordingListener;
        this.index = indexSupplier.get();
        this.dataReader = dataReader;
        this.readContinuously = readContinuously;
        this.completeFuture = this.dataReader.submitReaderJob(new CallRecordTreeBuildingJob());
    }

    public List<Recording> getRecordings() {
        return recordings.values()
            .stream()
            .filter(RecordingState::isPublished)
            .map(Recording::new)
            .collect(Collectors.toList());
    }

    public ProcessMetadata getProcessMetadata() {
        return dataReader.getProcessMetadata();
    }

    @Override
    public void close() throws Exception {
        try {
            dataReader.close();
        } finally {
            index.close();
        }
    }

    public void subscribe(RecordingListener recordingListener) {
        listenerLock.lock();
        try {
            this.recordingListener = recordingListener;
            this.recordings.values().forEach(recordingState -> {
                if (recordingState.isPublished()) {
                    recordingListener.onRecordingUpdated(recordingState.toRecording());
                }
            });
        } finally {
            listenerLock.unlock();
        }
    }

    private class CallRecordTreeBuildingJob implements RecordingDataReaderJob {

        private final Backoff backoff = new FixedDelayBackoff(Duration.ofMillis(100));

        @Override
        public void onProcessMetadata(ProcessMetadata processMetadata) {

        }

        @Override
        public void onRecordingMetadata(RecordingMetadata recordingMetadata) {
            RecordingState recordingState = recordings.computeIfAbsent(
                recordingMetadata.getId(),
                () -> new RecordingState(
                    recordingMetadata,
                    index,
                    dataReader,
                    methods,
                    types)
            );
            recordingState.update(recordingMetadata);
        }

        @Override
        public void onType(Type type) {
            types.store(type.getId(), type);
        }

        @Override
        public void onMethod(Method method) {
            methods.store(method.getId(), method);
        }

        @Override
        public void onRecordedCalls(long address, RecordedMethodCallList recordedMethodCalls) {
            if (recordedMethodCalls.isEmpty()) {
                return;
            }
            int recordingId = recordedMethodCalls.getRecordingId();
            RecordingState recording = recordings.get(recordingId);
            if (recording == null) {
                return;
            }
            listenerLock.lock();
            try {
                recording.onNewRecordedCalls(address, recordedMethodCalls);
                if (recording.isPublished()) {
                    recordingListener.onRecordingUpdated(recording.toRecording());
                } else {
                    Recording converted = recording.toRecording();
                    if (recording.getRoot() != null && true/*settings.getFilter().shouldPublish(converted)*/ && recording.publish()) {
                        recordingListener.onRecordingUpdated(converted);
                    }
                }
            } finally {
                listenerLock.unlock();
            }
        }

        @Override
        public boolean continueOnNoData() {
            if (readContinuously) {
                try {
                    backoff.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new StorageException("Interrupted", e);
                }
                return true;
            } else {
                return false;
            }
        }
    }
}
