package com.ulyp.storage.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ulyp.core.Method;
import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.RecordedMethodCall;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.Type;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.storage.reader.RecordingDataReaderJob;

import lombok.Getter;

public class StubRecordingDataReaderJob implements RecordingDataReaderJob {

    @Getter
    private ProcessMetadata processMetadata;
    @Getter
    private final Map<Integer, RecordingMetadata> recordingMetadatas = new HashMap<>();
    @Getter
    private final Map<Integer, Type> types = new HashMap<>();
    @Getter
    private final Map<Integer, Method> methods = new HashMap<>();
    @Getter
    private final Map<Integer, List<RecordedMethodCall>> recordedCalls = new HashMap<>();

    @Override
    public void onProcessMetadata(ProcessMetadata processMetadata) {
        this.processMetadata = processMetadata;
    }

    @Override
    public void onRecordingMetadata(RecordingMetadata recordingMetadata) {
        recordingMetadatas.put(recordingMetadata.getId(), recordingMetadata);
    }

    @Override
    public void onTypes(TypeList types) {
        types.forEach(type -> this.types.put(type.getId(), type));
    }

    @Override
    public void onMethods(MethodList methods) {
        methods.forEach(method -> this.methods.put(method.getId(), method));
    }

    @Override
    public void onRecordedCalls(long address, RecordedMethodCallList recordedMethodCalls) {
        int recordingId = recordedMethodCalls.getRecordingId();
        List<RecordedMethodCall> calls = recordedCalls.computeIfAbsent(recordingId, recId -> new ArrayList<>());
        for (RecordedMethodCall call : recordedMethodCalls) {
            calls.add(call);
        }
    }

    @Override
    public boolean continueOnNoData() {
        return false;
    }
}
