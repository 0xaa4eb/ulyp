package com.ulyp.storage.tree;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
import com.ulyp.storage.Index;
import com.ulyp.storage.Recording;
import com.ulyp.storage.RecordingDataReader;
import com.ulyp.storage.RecordingDataReaderJob;
import com.ulyp.storage.RecordingListener;
import com.ulyp.storage.StorageException;

import lombok.Getter;

/**
 * Primary call tree implementation which is used by tests and UI (thus is located here)
 */
public class CallRecordTree {

    private final RecordingDataReader dataReader;
    @Getter
    private final CompletableFuture<ProcessMetadata> processMetadataFuture = new CompletableFuture<>();
    private final CompletableFuture<Boolean> finishedReadingFuture = new CompletableFuture<>();
    private final InMemoryRepository<Integer, Type> types = new InMemoryRepository<>();
    private final Repository<Integer, Method> methods = new InMemoryRepository<>();
    private final InMemoryRepository<Integer, RecordingState> recordings = new InMemoryRepository<>();
    private final Index index;
    private final RecordingListener recordingListener;

    public CallRecordTree(RecordingDataReader dataReader, RecordingListener recordingListener, Supplier<Index> indexSupplier) {
        this.recordingListener = recordingListener;
        this.index = indexSupplier.get();
        this.dataReader = dataReader;
        this.dataReader.submitJob(new CallRecordTreeBuildingJob());
    }

    CompletableFuture<Boolean> getFinishedReadingFuture() {
        return finishedReadingFuture;
    }

    public List<Recording> getRecordings() {
        return recordings.values()
            .stream()
            .filter(RecordingState::isPublished)
            .map(Recording::new)
            .collect(Collectors.toList());
    }

    private class CallRecordTreeBuildingJob implements RecordingDataReaderJob {

        private final Backoff backoff = new FixedDelayBackoff(Duration.ofMillis(100));

        @Override
        public void onProcessMetadata(ProcessMetadata processMetadata) {
            processMetadataFuture.complete(processMetadata);
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
        public void onTypes(TypeList newTypes) {
            newTypes.forEach(type -> types.store(type.getId(), type));
        }

        @Override
        public void onMethods(MethodList newMethods) {
            newMethods.forEach(type -> methods.store(type.getId(), type));
        }

        @Override
        public void onRecordedCalls(long address, RecordedMethodCallList recordedMethodCalls) {
            if (recordedMethodCalls.isEmpty()) {
                return;
            }
            int recordingId = recordedMethodCalls.iterator().next().getRecordingId();
            RecordingState recording = recordings.get(recordingId);
            if (recording == null) {
                return;
            }
            recording.onNewRecordedCalls(address, recordedMethodCalls);
            if (recording.isPublished()) {
                recordingListener.onRecordingUpdated(recording.toRecording());
            } else {
                Recording converted = recording.toRecording();
                if (recording.getRoot() != null && true/*settings.getFilter().shouldPublish(converted)*/ && recording.publish()) {
                    recordingListener.onRecordingUpdated(converted);
                }
            }
        }

        @Override
        public boolean continueOnNoData() {
            try {
                backoff.await();
            } catch (InterruptedException e) {
                throw new StorageException("Interrupted", e);
            }
            return true;
        }

        @Override
        public void onComplete() {
            finishedReadingFuture.complete(true);
        }

        @Override
        public void close() throws Exception {
            finishedReadingFuture.complete(true);
        }
    }
}
