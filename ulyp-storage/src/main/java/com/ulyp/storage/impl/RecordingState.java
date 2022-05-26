package com.ulyp.storage.impl;

import com.ulyp.core.*;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.repository.ReadableRepository;
import com.ulyp.core.repository.Repository;
import com.ulyp.storage.CallRecord;
import com.ulyp.storage.Recording;
import com.ulyp.storage.RecordingListener;
import com.ulyp.storage.StorageException;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class RecordingState implements Closeable {

    private final DataReader reader;
    private final Repository<Long, RecordedCallState> index;
    private final MemCallStack memCallStack = new MemCallStack();
    private final ReadableRepository<Long, Method> methodRepository;
    private final ReadableRepository<Long, Type> typeRepository;
    private final RecordingListener listener;
    private final RecordingMetadata metadata;

    private long rootCallId = -1;

    public RecordingState(
            RecordingMetadata metadata,
            Repository<Long, RecordedCallState> index,
            DataReader dataReader,
            ReadableRepository<Long, Method> methodRepository,
            ReadableRepository<Long, Type> typeRepository,
            RecordingListener recordingListener) {
        this.index = index;
        this.metadata = metadata;
        this.reader = dataReader;
        this.methodRepository = methodRepository;
        this.typeRepository = typeRepository;
        this.listener = recordingListener;
    }

    void onNewRecordedCalls(long fileAddr, RecordedMethodCallList calls) {
        synchronized (this) {
            AddressableItemIterator<RecordedMethodCall> iterator = calls.iterator();
            while (iterator.hasNext()) {
                RecordedMethodCall value = iterator.next();
                long relativeAddress = iterator.address();
                if (value instanceof RecordedEnterMethodCall) {
                    if (rootCallId < 0) {
                        rootCallId = value.getCallId();
                    }
                    RecordedCallState callState = RecordedCallState.builder()
                            .callId(value.getCallId())
                            .enterMethodCallAddr(fileAddr + relativeAddress)
                            .build();
                    memCallStack.push(callState);
                } else {

                    RecordedCallState lastCallState = memCallStack.peek();
                    if (lastCallState == null || lastCallState.getCallId() != value.getCallId()) {
                        throw new StorageException("Inconsistent recording file. The last recorded enter method call has different " +
                                "call id rather than the last exit method call. This usually happens when recording of constructors is enabled, and" +
                                " an exception is thrown inside a consutructor. Please disable recording constructors (-Dulyp.constructors option)");
                    }

                    memCallStack.pop();
                    lastCallState.setExitMethodCallAddr(fileAddr + relativeAddress);
                    index.store(lastCallState.getCallId(), lastCallState);
                }
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
        if (metadata.getRecordingCompletedEpochMillis() > 0) {
            this.metadata.setRecordingCompletedEpochMillis(metadata.getRecordingCompletedEpochMillis());
        }
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

        CallRecord.CallRecordBuilder builder = CallRecord.builder()
                .callId(callState.getCallId())
                .subtreeSize(callState.getSubtreeSize())
                .childrenCallIds(callState.getChildrenCallIds())
                .method(methodRepository.get(enterMethodCall.getMethodId()))
                .callee(enterMethodCall.getCallee().toRecord(typeRepository))
                .args(new ArrayList<>(enterMethodCall.getArguments().stream()
                        .map(recorded -> recorded.toRecord(typeRepository))
                        .collect(Collectors.toList())))
                .recordingState(this);

        if (callState.getExitMethodCallAddr() > 0) {
            RecordedExitMethodCall exitMethodCall = reader.readExitMethodCall(callState.getExitMethodCallAddr());

            builder = builder.thrown(exitMethodCall.isThrown())
                    .returnValue(exitMethodCall.getReturnValue().toRecord(typeRepository));
        }

        return builder.build();
    }

    public synchronized RecordingMetadata getMetadata() {
        return metadata;
    }

    public synchronized Duration getLifetime() {
        RecordingMetadata metadata = getMetadata();
        if (metadata.getRecordingCompletedEpochMillis() > 0) {
            return Duration.ofMillis(metadata.getRecordingCompletedEpochMillis() - metadata.getRecordingStartedEpochMillis());
        } else {
            return Duration.ofSeconds(0);
        }
    }

    public synchronized int callCount() {
        return rootCallId >= 0 ? getRoot().getSubtreeSize() : 0;
    }

    @Override
    public synchronized void close() throws IOException {
        reader.close();
    }
}
