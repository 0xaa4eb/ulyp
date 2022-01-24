package com.ulyp.storage;

import com.ulyp.core.Method;
import com.ulyp.core.recorders.NotRecordedObjectRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.storage.impl.RecordingState;
import it.unimi.dsi.fastutil.longs.LongList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Method call record which was deserialized from binary format into POJO. Stands for a particular
 * method call in some recording session.
 *
 * Call records are managed as tree nodes. Let's say there is the following method being recorded:
 *
 * public int a() {
 *     b();
 *     c();
 *     return 2;
 * }
 *
 * If methods b() and c() do not have any method calls in them, then there should be 3 call record nodes in
 * total. The root is a() call which have children call records for b() and c().
 *
 * Every call record node has unique (within recording session) identifier which always starts from 0.
 * Thus a() call record will have id 0, b() will have 1, etc
 *
 * Sometimes call records may not be complete, in that case it's not possible to know, what method returned
 * and whether it thrown any exception or not. This happens when method exit has not yet been recorded (i.e.
 * method exit still hasn't happened or app crashed during recording session)
 */
public class CallRecord {

    private final long callId;
    private final int subtreeSize;
    private final Method method;
    private final ObjectRecord callee;
    private final List<ObjectRecord> args;
    private ObjectRecord returnValue = NotRecordedObjectRecord.getInstance();
    private boolean thrown;
    private final LongList childrenCallIds;
    private final RecordingState recordingState;

    private List<CallRecord> children = null;

    public CallRecord(
            long callId,
            int subtreeSize,
            LongList childrenCallIds,
            ObjectRecord callee,
            List<ObjectRecord> args,
            Method method,
            RecordingState recordingState)
    {
        this.callId = callId;
        this.subtreeSize = subtreeSize;
        this.callee = callee;
        this.args = new ArrayList<>(args);
        this.method = method;
        this.childrenCallIds = childrenCallIds;
        this.recordingState = recordingState;
    }

    public int getSubtreeSize() {
        return subtreeSize;
    }

    public ObjectRecord getCallee() {
        return callee;
    }

    public long getId() {
        return callId;
    }

    public boolean isVoidMethod() {
        return !method.returnsSomething();
    }

    public boolean isConstructor() {
        return method.isConstructor();
    }

    public boolean isStatic() {
        return method.isStatic();
    }

    public String getClassName() {
        return method.getDeclaringType().getName();
    }

    public String getMethodName() {
        return method.getName();
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

    public boolean isComplete() {
        return returnValue != NotRecordedObjectRecord.getInstance();
    }

    public void setReturnValue(ObjectRecord returnValue) {
        this.returnValue = returnValue;
    }

    public void setThrown(boolean thrown) {
        this.thrown = thrown;
    }

    public String toString() {
        return getReturnValue() + " : " +
                getMethod().getDeclaringType().getName() +
                "." +
                getMethod().getName() +
                args;
    }
}
