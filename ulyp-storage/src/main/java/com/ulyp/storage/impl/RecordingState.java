package com.ulyp.storage.impl;

import com.ulyp.core.*;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.repository.InMemoryRepository;
import com.ulyp.storage.CallRecord;
import com.ulyp.core.repository.ReadableRepository;
import com.ulyp.core.repository.Repository;
import com.ulyp.storage.Recording;
import com.ulyp.storage.RecordingListener;

import java.io.Closeable;
import java.io.IOException;
import java.util.stream.Collectors;

public class RecordingState implements Closeable {

    private final DataReader reader;
    private final Repository<Long, RecordedCallState> index = new InMemoryRepository<>();
    private final MemCallStack memCallStack = new MemCallStack();
    private final ReadableRepository<Long, Method> methodRepository;
    private final ReadableRepository<Long, Type> typeRepository;
    private final RecordingListener listener;

    private RecordingMetadata metadata;

    private long rootCallId = -1;

    public RecordingState(
            RecordingMetadata metadata,
            DataReader dataReader,
            ReadableRepository<Long, Method> methodRepository,
            ReadableRepository<Long, Type> typeRepository,
            RecordingListener recordingListener)
    {
        this.metadata = metadata;
        this.reader = dataReader;
        this.methodRepository = methodRepository;
        this.typeRepository = typeRepository;
        this.listener = recordingListener;
    }

    synchronized void onRecordedCalls(long fileAddr, RecordedMethodCallList calls) {

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

        listener.onRecordingUpdated(this.toRecording());
    }

    private synchronized Recording toRecording() {
        return new Recording(this);
    }

    public synchronized RecordedCallState getState(long callId) {
        RecordedCallState callState = memCallStack.get(callId);
        if (callState != null) {
            return callState;
        }
        return index.get(callId);
    }

    public synchronized int getId() {
        return metadata.getId();
    }

    public synchronized void update(RecordingMetadata metadata) {
        // TODO
    }

    public synchronized CallRecord getRoot() {
        return getCallRecord(rootCallId);
    }

    public synchronized CallRecord getCallRecord(long callId) {
        if (callId < 0) {
            return null;
        }

        RecordedCallState callState = getState(callId);
        RecordedEnterMethodCall enterMethodCall = reader.readEnterMethodCall(callState.getEnterMethodCallAddr());

        CallRecord callRecord = new CallRecord(
                callState.getCallId(),
                callState.getSubtreeSize(),
                callState.getChildrenCallIds(),
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

    public synchronized RecordingMetadata getMetadata() {
        return metadata;
    }

    public synchronized int callCount() {
        return rootCallId >= 0 ? getRoot().getSubtreeSize() : 0;
    }

    @Override
    public synchronized void close() throws IOException {
        reader.close();
    }
}
