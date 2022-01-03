package com.ulyp.agent.transport;

import com.ulyp.core.CallRecordLog;
import com.ulyp.core.Method;
import com.ulyp.core.Type;
import com.ulyp.core.process.ProcessInfo;

import java.util.Collection;

public class CallRecordTreeRequest {

    private final CallRecordLog recordLog;
    private final Collection<Method> methods;
    private final Collection<Type> types;
    private final ProcessInfo processInfo;
    private final long endLifetimeEpochMillis = System.currentTimeMillis();

    public CallRecordTreeRequest(CallRecordLog recordLog, Collection<Method> methods, Collection<Type> types, ProcessInfo processInfo) {
        this.recordLog = recordLog;
        this.methods = methods;
        this.types = types;
        this.processInfo = processInfo;
    }

    public CallRecordLog getRecordLog() {
        return recordLog;
    }

    public Collection<Method> getMethods() {
        return methods;
    }

    public Collection<Type> getTypes() {
        return types;
    }

    public ProcessInfo getProcessInfo() {
        return processInfo;
    }

    public long getEndLifetimeEpochMillis() {
        return endLifetimeEpochMillis;
    }
}
