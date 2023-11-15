package com.perf.agent.benchmarks.proc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordedExitMethodCall;
import com.ulyp.core.RecordedMethodCall;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.storage.reader.RecordingDataReader;
import com.ulyp.storage.reader.RecordingDataReaderJob;

public class RecordingResult {

    private final Map<Integer, RecordingMetadata> recordingMetadataMap = new HashMap<>();
    private final Map<Integer, Long> recordingCallCountMap = new HashMap<>();

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

    public Collection<RecordingMetadata> getRecordingMetadataMap() {
        return recordingMetadataMap.values();
    }

    public long getRecordedCalls(int recordingId) {
        return recordingCallCountMap.get(recordingId);
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
            int recordingId = recordedCalls.getRecordingId();
            recordingCallCountMap.putIfAbsent(recordingId, 0L);

            int calls = 0;
            for (RecordedMethodCall call : recordedCalls) {
                if (call instanceof RecordedExitMethodCall) {
                    calls++;
                }
            }

            recordingCallCountMap.put(recordingId, recordingCallCountMap.get(recordingId) + calls);
        }

        @Override
        public boolean continueOnNoData() {
            return false;
        }
    }
}
