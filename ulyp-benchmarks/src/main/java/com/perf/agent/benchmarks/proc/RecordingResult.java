package com.perf.agent.benchmarks.proc;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.storage.RecordingDataReader;
import com.ulyp.storage.RecordingDataReaderJob;

public class RecordingResult {

    private final Map<Integer, RecordingMetadata> recordingMetadataMap = new ConcurrentHashMap<>();

    public RecordingResult(RecordingDataReader recordingDataReader) {
        CompletableFuture<Void> complete = recordingDataReader.submitReaderJob(new RecordingResultBuilderJob());
        try {
            complete.get(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public int getRecordingsCount() {
        return recordingMetadataMap.size();
    }

    public RecordingMetadata getRecordingMetadata(int id) {
        return recordingMetadataMap.get(id);
    }

    private class RecordingResultBuilderJob implements RecordingDataReaderJob {

        @Override
        public void onProcessMetadata(ProcessMetadata processMetadata) {

        }

        @Override
        public void onRecordingMetadata(RecordingMetadata recordingMetadata) {
            recordingMetadataMap.put(recordingMetadata.getId(), recordingMetadata);
        }

        @Override
        public void onTypes(TypeList types) {

        }

        @Override
        public void onMethods(MethodList methods) {

        }

        @Override
        public void onRecordedCalls(long address, RecordedMethodCallList recordedCalls) {

        }

        @Override
        public boolean continueOnNoData() {
            return false;
        }
    }
}
