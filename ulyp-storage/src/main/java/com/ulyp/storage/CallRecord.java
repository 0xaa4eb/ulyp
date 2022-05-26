package com.ulyp.storage;

import com.ulyp.core.Method;
import com.ulyp.core.recorders.NotRecordedObjectRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.storage.impl.RecordingState;
import it.unimi.dsi.fastutil.longs.LongList;
import lombok.Builder;

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
    private final int subtreeSize;
    private final Method method;
    private final ObjectRecord callee;
    private final List<ObjectRecord> args;
    private final LongList childrenCallIds;
    private final RecordingState recordingState;
    @Builder.Default
    private final boolean thrown = false;
    @Builder.Default
    private final ObjectRecord returnValue = NotRecordedObjectRecord.getInstance();

    private List<CallRecord> children;

    public int getSubtreeSize() {
        return subtreeSize;
    }

    public ObjectRecord getCallee() {
        return callee;
    }

    public long getId() {
        return callId;
    }

    public LongList getChildrenCallIds() {
        return childrenCallIds;
    }

    public long getCallId() {
        return callId;
    }

    public List<ObjectRecord> getArgs() {
        return args;
    }

    public ObjectRecord getReturnValue() {
        return returnValue;
    }

    public boolean hasThrown() {
        return thrown;
    }

    public Method getMethod() {
        return method;
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
                getMethod().getDeclaringType().getName() +
                "." +
                getMethod().getName() +
                args;
    }
}
