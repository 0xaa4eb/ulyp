package com.ulyp.core;

import com.ulyp.core.mem.RecordedMethodCallList;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.NotThreadSafe;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

/**
 * A collection of enter and exit recorded method calls for a certain recording session.
 */
@NotThreadSafe
@Slf4j
public class CallRecordBuffer {

    public static final AtomicInteger idGenerator = new AtomicInteger(-1);

    private final TypeResolver typeResolver;
    private final RecordedMethodCallList recordedCalls = new RecordedMethodCallList();
    private final long rootCallId;

    private final RecordingMetadata recordingMetadata;
    private long lastExitCallId = -1;
    private long nextCallId;

    public CallRecordBuffer(TypeResolver typeResolver, long callIdInitialValue) {
        List<String> stackTraceElements = Stream.of(new Exception().getStackTrace())
            .skip(4)
            .map(StackTraceElement::toString)
            .collect(Collectors.toList());

        this.recordingMetadata = RecordingMetadata.builder()
                .id(idGenerator.incrementAndGet())
                .recordingStartedEpochMillis(System.currentTimeMillis())
                .logCreatedEpochMillis(System.currentTimeMillis())
                .threadId(Thread.currentThread().getId())
                .threadName(Thread.currentThread().getName())
                .stackTraceElements(stackTraceElements)
                .build();

        this.typeResolver = typeResolver;
        this.nextCallId = callIdInitialValue;
        this.rootCallId = callIdInitialValue;
    }

    private CallRecordBuffer(
            int id,
            TypeResolver typeResolver,
            long nextCallId,
            long rootCallId) {
        this.recordingMetadata = RecordingMetadata.builder()
                .id(id)
                .logCreatedEpochMillis(System.currentTimeMillis())
                .threadId(Thread.currentThread().getId())
                .threadName(Thread.currentThread().getName())
                .build();

        this.typeResolver = typeResolver;
        this.nextCallId = nextCallId;
        this.rootCallId = rootCallId;
    }

    public CallRecordBuffer cloneWithoutData() {
        return new CallRecordBuffer(this.recordingMetadata.getId(), this.typeResolver, this.nextCallId, rootCallId);
    }

    public long estimateBytesSize() {
        return recordedCalls.getRawBytes().byteLength();
    }

    public long onMethodEnter(Method method, @Nullable Object callee, Object[] args) {
        try {

            long callId = nextCallId++;
            recordedCalls.addEnterMethodCall(
                    recordingMetadata.getId(),
                    callId,
                    method,
                    typeResolver,
                    callee,
                    args
            );
            return callId;
        } catch (Throwable err) {
            // catch Throwable intentionally. While recording is done anything can happen, but the app which uses ulyp should not be disrupted
            log.error("Error while recording", err);
            return -1;
        }
    }

    public void onMethodExit(Method method, Object returnValue, Throwable thrown, long callId) {
        if (callId >= 0) {
            if (thrown == null) {
                recordedCalls.addExitMethodCall(
                    recordingMetadata.getId(),
                    callId,
                    method,
                    typeResolver,
                    false,
                    returnValue
                );
            } else {
                recordedCalls.addExitMethodCall(
                    recordingMetadata.getId(),
                    callId,
                    method,
                    typeResolver,
                    true,
                    thrown
                );
            }
            lastExitCallId = callId;
            if (isComplete()) {
                this.recordingMetadata.setRecordingCompletedEpochMillis(System.currentTimeMillis());
            }
        }
    }

    public boolean isComplete() {
        return lastExitCallId == rootCallId;
    }

    public RecordingMetadata getRecordingMetadata() {
        return recordingMetadata;
    }

    public long getTotalRecordedEnterCalls() {
        return nextCallId - rootCallId;
    }

    public int getRecordedCallsSize() {
        return recordedCalls.size();
    }

    public RecordedMethodCallList getRecordedCalls() {
        return recordedCalls;
    }

    @Override
    public String toString() {
        return "CallRecordBuffer{id=" + recordingMetadata + '}';
    }
}