package com.ulyp.core;

import com.ulyp.core.recorders.NotRecordedObjectRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.database.DatabaseException;
import com.ulyp.transport.BooleanType;
import com.ulyp.transport.TMethodInfoDecoder;

import java.util.ArrayList;
import java.util.List;

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

    private final long id;
    private final String className;

    // TODO move this group to method class
    private final String methodName;
    private final boolean isVoidMethod;
    private final boolean isStatic;
    private final boolean isConstructor;
    private final List<String> parameterNames;

    private final ObjectRecord callee;
    private final List<ObjectRecord> args;

    private ObjectRecord returnValue = NotRecordedObjectRecord.getInstance();
    private boolean thrown;

    private final CallRecordDatabase database;

    public CallRecord(
            long id,
            ObjectRecord callee,
            List<ObjectRecord> args,
            TMethodInfoDecoder methodDescription,
            CallRecordDatabase database)
    {
        this.id = id;
        this.callee = callee;
        this.isVoidMethod = methodDescription.returnsSomething() == BooleanType.F;
        this.isStatic = methodDescription.staticFlag() == BooleanType.T;
        this.isConstructor = methodDescription.constructor() == BooleanType.T;
        this.args = new ArrayList<>(args);
        int originalLimit = methodDescription.limit();

        TMethodInfoDecoder.ParameterNamesDecoder paramNamesDecoder = methodDescription.parameterNames();
        this.parameterNames = new ArrayList<>();
        while (paramNamesDecoder.hasNext()) {
            this.parameterNames.add(paramNamesDecoder.next().value());
        }
        this.className = methodDescription.className();
        this.methodName = methodDescription.methodName();
        methodDescription.limit(originalLimit);

        this.database = database;
    }

    public ObjectRecord getCallee() {
        return callee;
    }

    public long getId() {
        return id;
    }

    public boolean isVoidMethod() {
        return isVoidMethod;
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public long getSubtreeNodeCount() throws DatabaseException {
        return database.getSubtreeCount(id);
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<ObjectRecord> getArgs() {
        return args;
    }

    public List<String> getParameterNames() {
        return parameterNames;
    }

    public ObjectRecord getReturnValue() {
        return returnValue;
    }

    public boolean hasThrown() {
        return thrown;
    }

    public List<CallRecord> getChildren() throws DatabaseException {
        return database.getChildren(this.id);
    }

    public void setReturnValue(ObjectRecord returnValue) {
        this.returnValue = returnValue;
    }

    public void setThrown(boolean thrown) {
        this.thrown = thrown;
    }

    public String toString() {
        return getReturnValue() + " : " +
                className +
                "." +
                methodName +
                args;
    }

    public boolean isComplete() {
        return returnValue != NotRecordedObjectRecord.getInstance();
    }
}
