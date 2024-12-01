package com.ulyp.storage.search;

import com.ulyp.core.*;
import com.ulyp.core.repository.InMemoryRepository;
import com.ulyp.core.repository.Repository;
import com.ulyp.storage.reader.RecordedMethodCalls;
import com.ulyp.storage.reader.RecordingDataReaderJob;

public class SearchDataReaderJob implements RecordingDataReaderJob {

    private final SearchQuery query;
    private final SearchResultListener resultListener;
    private final InMemoryRepository<Integer, Type> types = new InMemoryRepository<>();
    private final Repository<Integer, Method> methods = new InMemoryRepository<>();

    public SearchDataReaderJob(SearchQuery query, SearchResultListener resultListener) {
        this.query = query;
        this.resultListener = resultListener;
    }

    @Override
    public void onStart() {
        resultListener.onStart();
    }

    @Override
    public void onProcessMetadata(ProcessMetadata processMetadata) {

    }

    @Override
    public void onRecordingMetadata(RecordingMetadata recordingMetadata) {

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
    public void onRecordedCalls(long address, RecordedMethodCalls recordedMethodCalls) {
        if (recordedMethodCalls.isEmpty()) {
            return;
        }

        AddressableItemIterator<RecordedMethodCall> it = recordedMethodCalls.iterator(types);
        while (it.hasNext()) {
            RecordedMethodCall methodCall = it.next();

            if (methodCall instanceof RecordedEnterMethodCall) {
                RecordedEnterMethodCall enterMethodCall = (RecordedEnterMethodCall) methodCall;

                if (query.matches(enterMethodCall, types, methods)) {
                    resultListener.onMatch(recordedMethodCalls.getRecordingId(), enterMethodCall);
                }
            } else {
                RecordedExitMethodCall exitMethodCall = (RecordedExitMethodCall) methodCall;

                if (query.matches(exitMethodCall, types, methods)) {
                    resultListener.onMatch(recordedMethodCalls.getRecordingId(), exitMethodCall);
                }
            }
        }
    }

    @Override
    public boolean continueOnNoData() {
        return false;
    }
}
