package com.ulyp.storage.impl;

import com.ulyp.core.*;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.storage.CallRecord;
import com.ulyp.core.ReadableRepository;
import com.ulyp.core.Repository;

import java.util.stream.Collectors;

public class RecordingState {

    private final RecordingMetadata recordingMetadata;
    private final DataReader reader;
    private final Repository<RecordedCallState> index = new InMemoryRepository<>();
    private final MemCallStack memCallStack = new MemCallStack();
    private final ReadableRepository<Method> methodRepository;
    private final ReadableRepository<Type> typeRepository;

    private long rootCallId = -1;

    public RecordingState(
            RecordingMetadata recordingMetadata,
            DataReader dataReader,
            ReadableRepository<Method> methodRepository,
            ReadableRepository<Type> typeRepository)
    {
        this.recordingMetadata = recordingMetadata;
        this.reader = dataReader;
        this.methodRepository = methodRepository;
        this.typeRepository = typeRepository;
    }

    void onRecordedCalls(long fileAddr, RecordedMethodCallList calls) {

        AddressableItemIterator<RecordedMethodCall> iterator = calls.iterator();
        while (iterator.hasNext()) {
            RecordedMethodCall value = iterator.next();
            long relativeAddress = iterator.address();
            if (value instanceof RecordedEnterMethodCall) {
                if (rootCallId < 0) {
                    rootCallId = value.getCallId();
                }
                RecordedCallState callState = new RecordedCallState(
                        value.getCallId(),
                        fileAddr + relativeAddress
                );
                memCallStack.push(callState);
            } else {

                RecordedCallState callState = memCallStack.pop();
                callState.setExitMethodCallAddr(fileAddr + relativeAddress);
                index.store(callState.getCallId(), callState);
            }
        }
    }

    public RecordedCallState getState(long callId) {
        RecordedCallState callState = memCallStack.get(callId);
        if (callState != null) {
            return callState;
        }
        return index.get(callId);
    }

    public int getId() {
        return recordingMetadata.getId();
    }

    public void update(RecordingMetadata metadata) {
        // TODO
    }

    public CallRecord getRoot() {
        if (rootCallId < 0) {
            return null;
        }

        RecordedCallState callState = getState(rootCallId);
        RecordedEnterMethodCall enterMethodCall = reader.readEnterMethodCall(callState.getEnterMethodCallAddr());

        CallRecord callRecord = new CallRecord(
                callState.getCallId(),
                enterMethodCall.getCallee().toRecord(typeRepository),
                enterMethodCall.getArguments().stream()
                        .map(recorded -> recorded.toRecord(typeRepository))
                        .collect(Collectors.toList()),
                methodRepository.get(enterMethodCall.getMethodId()),
                this
        );

        if (callState.getExitMethodCallAddr() > 0) {
            RecordedExitMethodCall exitMethodCall = reader.readExitMethodCall(callState.getExitMethodCallAddr());

            callRecord.setThrown(exitMethodCall.isThrown());
            callRecord.setReturnValue(exitMethodCall.getReturnValue().toRecord(typeRepository));
        }

        return callRecord;
    }
}
