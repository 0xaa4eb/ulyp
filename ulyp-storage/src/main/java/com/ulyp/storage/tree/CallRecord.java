package com.ulyp.storage.tree;

import com.ulyp.core.Method;
import com.ulyp.core.recorders.NotRecordedObjectRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.storage.StorageException;
import lombok.Builder;
import lombok.Getter;
import org.agrona.collections.LongArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Method call record which was deserialized from binary format into POJO. Stands for a particular
 * method call in some recording session.
 * <p>
 * Call records are managed as tree nodes. Let's say there is the following method being recorded:
 * <p>
 * public int a() {
 * b();
 * c();
 * return 2;
 * }
 * <p>
 * If methods b() and c() do not have any method calls in them, then there should be 3 call record nodes in
 * total. The root is a() call which have children call records for b() and c().
 * <p>
 * Every call record node has unique (within recording session) identifier which always starts from 0.
 * Thus a() call record will have id 0, b() will have 1, etc
 * <p>
 * Sometimes call records may not be complete, in that case it's not possible to know, what method returned
 * and whether it thrown any exception or not. This happens when method exit has not yet been recorded (i.e.
 * method exit still hasn't happened or app crashed during recording session)
 */
@Builder
public class CallRecord {

    private final long callId;
    @Getter
    private final int subtreeSize;
    @Getter
    private final long nanosDuration;
    @Getter
    private final Method method;
    @Getter
    private final List<ObjectRecord> args;
    @Getter
    private final LongArrayList childrenCallIds;
    private final RecordingState recordingState;
    @Builder.Default
    private final ObjectRecord callee = NotRecordedObjectRecord.getInstance();
    @Builder.Default
    private final boolean thrown = false;
    @Builder.Default
    private final ObjectRecord returnValue = NotRecordedObjectRecord.getInstance();

    private List<CallRecord> children;

    @NotNull
    public ObjectRecord getCallee() {
        if (method.isConstructor()) {
            return returnValue;
        } else {
            return callee;
        }
    }

    public long getId() {
        return callId;
    }

    @NotNull
    public ObjectRecord getReturnValue() {
        return returnValue;
    }

    public boolean hasThrown() {
        return thrown;
    }

    public List<CallRecord> getChildren() throws StorageException {
        if (children != null) {
            return children;
        }

        return children = childrenCallIds.stream()
                .map(recordingState::getCallRecord)
                .collect(Collectors.toList());
    }

    public boolean isFullyRecorded() {
        return returnValue != NotRecordedObjectRecord.getInstance();
    }

    public String toString() {
        return getReturnValue() + " : " +
                getMethod().getType().getName() +
                "." +
                getMethod().getName() +
                args;
    }
}
