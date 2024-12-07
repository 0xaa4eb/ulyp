package com.ulyp.storage.util;

import com.ulyp.core.*;
import com.ulyp.core.repository.InMemoryRepository;
import com.ulyp.storage.reader.RecordedMethodCalls;
import com.ulyp.storage.reader.RecordingDataReaderJob;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class StubRecordingDataReaderJob implements RecordingDataReaderJob {

    private ProcessMetadata processMetadata;
    private final Map<Integer, RecordingMetadata> recordingMetadatas = new HashMap<>();
    private final Map<Integer, Method> methods = new HashMap<>();
    private final Map<Integer, List<RecordedMethodCall>> recordedCalls = new HashMap<>();
    private final InMemoryRepository<Integer, Type> types = new InMemoryRepository<>();

    @Override
    public void onProcessMetadata(ProcessMetadata processMetadata) {
        this.processMetadata = processMetadata;
    }

    @Override
    public void onRecordingMetadata(RecordingMetadata recordingMetadata) {
        recordingMetadatas.put(recordingMetadata.getId(), recordingMetadata);
    }

    @Override
    public void onType(Type type) {
        this.types.store(type.getId(), type);
    }

    @Override
    public void onMethod(Method method) {
        this.methods.put(method.getId(), method);
    }

    @Override
    public void onRecordedCalls(long address, RecordedMethodCalls recordedMethodCalls) {
        int recordingId = recordedMethodCalls.getRecordingId();
        List<RecordedMethodCall> calls = recordedCalls.computeIfAbsent(recordingId, recId -> new ArrayList<>());
        AddressableItemIterator<RecordedMethodCall> iterator = recordedMethodCalls.iterator(types);
        while (iterator.hasNext()) {
            calls.add(iterator.next());
        }
    }

    @Override
    public boolean continueOnNoData() {
        return false;
    }
}
