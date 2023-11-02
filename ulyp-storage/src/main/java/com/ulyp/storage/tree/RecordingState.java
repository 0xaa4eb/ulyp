package com.ulyp.storage.tree;

import com.ulyp.core.*;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.repository.ReadableRepository;
import com.ulyp.core.util.BitUtil;
import com.ulyp.storage.*;
import com.ulyp.storage.impl.MemCallStack;

import lombok.Getter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class RecordingState {

    private final R recordingDataReader;
    private final Index index;
    private final MemCallStack memCallStack = new MemCallStack();
    private final ReadableRepository<Integer, Method> methodRepository;
    private final ReadableRepository<Integer, Type> typeRepository;
    private final RecordingMetadata metadata;
    @Getter
    private volatile boolean published = false;

    private long rootUniqueId = -1;

    public RecordingState(
            RecordingMetadata metadata,
            Index index,
            R recordingDataReader,
            ReadableRepository<Integer, Method> methodRepository,
            ReadableRepository<Integer, Type> typeRepository) {
        this.index = index;
        this.metadata = metadata;
        this.recordingDataReader = recordingDataReader;
        this.methodRepository = methodRepository;
        this.typeRepository = typeRepository;
    }

    public synchronized void onNewRecordedCalls(long fileAddr, RecordedMethodCallList calls) {
        AddressableItemIterator<RecordedMethodCall> iterator = calls.iterator();
        while (iterator.hasNext()) {
            RecordedMethodCall value = iterator.next();
            long relativeAddress = iterator.address();
            long uniqueId = BitUtil.longFromInts(metadata.getId(), (int) value.getCallId());
            if (value instanceof RecordedEnterMethodCall) {
                if (rootUniqueId < 0) {
                    rootUniqueId = uniqueId;
                }
                RecordedCallState callState = RecordedCallState.builder()
                    .id(uniqueId)
                    .enterMethodCallAddr(fileAddr + relativeAddress)
                    .build();
                memCallStack.push(callState);
            } else {

                RecordedCallState lastCallState = memCallStack.peek();
                if (lastCallState == null || lastCallState.getId() != uniqueId) {
/*
                        throw new StorageException("Inconsistent recording file. The last recorded enter method call has different " +
                                "call id rather than the last exit method call. This usually happens when recording of constructors is enabled, and" +
                                " an exception is thrown inside a consutructor. Please disable recording constructors (-Dulyp.constructors option)");
*/
                    return;
                }

                memCallStack.pop();
                lastCallState.setExitMethodCallAddr(fileAddr + relativeAddress);
                index.store(lastCallState.getId(), lastCallState);
            }
        }
    }

    public synchronized boolean publish() {
        if (!published) {
            published = true;
            return true;
        }
        return false;
    }

    public synchronized Recording toRecording() {
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
        return getCallRecord(rootUniqueId);
    }

    public synchronized CallRecord getCallRecord(long callId) {
        if (callId < 0) {
            return null;
        }

        RecordedCallState callState = getState(callId);
        RecordedEnterMethodCall enterMethodCall = recordingDataReader.readEnterMethodCall(callState.getEnterMethodCallAddr());

        CallRecord.CallRecordBuilder builder = CallRecord.builder()
                .callId(callState.getId())
                .subtreeSize(callState.getSubtreeSize())
                .childrenCallIds(callState.getChildrenCallIds())
                .method(methodRepository.get(enterMethodCall.getMethodId()))
                .callee(enterMethodCall.getCallee().toRecord(typeRepository))
                .args(new ArrayList<>(enterMethodCall.getArguments().stream()
                        .map(recorded -> recorded.toRecord(typeRepository))
                        .collect(Collectors.toList())))
                .recordingState(this);

        if (callState.getExitMethodCallAddr() > 0) {
            RecordedExitMethodCall exitMethodCall = recordingDataReader.readExitMethodCall(callState.getExitMethodCallAddr());

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
        return rootUniqueId >= 0 ? getRoot().getSubtreeSize() : 0;
    }
}
