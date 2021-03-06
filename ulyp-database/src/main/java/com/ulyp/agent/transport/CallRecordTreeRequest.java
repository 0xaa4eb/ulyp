package com.ulyp.agent.transport;

import com.ulyp.core.CallRecordLog;
import com.ulyp.core.MethodInfo;
import com.ulyp.core.printers.TypeInfo;
import com.ulyp.core.process.ProcessInfo;

import java.util.Collection;

public class CallRecordTreeRequest {

    private final CallRecordLog recordLog;
    private final Collection<MethodInfo> methods;
    private final Collection<TypeInfo> typeInfos;
    private final ProcessInfo processInfo;
    private final long endLifetimeEpochMillis = System.currentTimeMillis();

    public CallRecordTreeRequest(CallRecordLog recordLog, Collection<MethodInfo> methods, Collection<TypeInfo> typeInfos, ProcessInfo processInfo) {
        this.recordLog = recordLog;
        this.methods = methods;
        this.typeInfos = typeInfos;
        this.processInfo = processInfo;
    }

    public CallRecordLog getRecordLog() {
        return recordLog;
    }

    public Collection<MethodInfo> getMethods() {
        return methods;
    }

    public Collection<TypeInfo> getTypes() {
        return typeInfos;
    }

    public ProcessInfo getProcessInfo() {
        return processInfo;
    }

    public long getEndLifetimeEpochMillis() {
        return endLifetimeEpochMillis;
    }
}
