package com.ulyp.agent;

import com.ulyp.agent.transport.CallRecordTreeRequest;
import com.ulyp.agent.util.EnhancedThreadLocal;
import com.ulyp.core.*;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
@ThreadSafe
public class Recorder {

    private static final Recorder instance = new Recorder(AgentContext.getInstance());

    /**
     * Keeps current recording session count. Based on the fact that most of the time there is no
     * recording sessions and this counter is equal to 0, it's possible to make a small performance optimization.
     * Advice code (see RecordingAdvice class) can first check if there are any recording sessions are active at all. If there are any,
     * then advice code will check thread local and know if there is recording session in this thread precisely.
     * This helps minimizing thread local lookups in the advice code
     */
    public static final AtomicInteger currentRecordingSessionCount = new AtomicInteger();

    public static Recorder getInstance() {
        return instance;
    }

    private final EnhancedThreadLocal<CallRecordLog> threadLocalRecordsLog = new EnhancedThreadLocal<>();
    private final AgentContext context;

    public Recorder(AgentContext context) {
        this.context = context;
    }

    public boolean recordingIsActiveInCurrentThread() {
        return threadLocalRecordsLog.get() != null;
    }

    public long startOrContinueRecordingOnMethodEnter(
            TypeResolver typeResolver,
            MethodInfo methodInfo,
            @Nullable Object callee,
            Object[] args)
    {
        CallRecordLog recordLog = threadLocalRecordsLog.getOrCreate(() -> {
            CallRecordLog log = new CallRecordLog(
                    typeResolver,
                    context.getSysPropsSettings().getMaxTreeDepth(),
                    context.getSysPropsSettings().getMaxCallsToRecordPerMethod());
            currentRecordingSessionCount.incrementAndGet();
            return log;
        });
        return onMethodEnter(methodInfo, callee, args);
    }

    public long startOrContinueRecordingOnConstructorEnter(
            TypeResolver typeResolver,
            MethodInfo methodInfo,
            Object[] args)
    {
        CallRecordLog recordLog = threadLocalRecordsLog.getOrCreate(() -> {
            CallRecordLog log = new CallRecordLog(
                    typeResolver,
                    context.getSysPropsSettings().getMaxTreeDepth(),
                    context.getSysPropsSettings().getMaxCallsToRecordPerMethod());
            currentRecordingSessionCount.incrementAndGet();
            return log;
        });
        return onConstructorEnter(methodInfo, args);
    }

    public void endRecordingIfPossibleOnMethodExit(TypeResolver typeResolver, MethodInfo methodInfo, Object result, Throwable thrown, long callId) {
        onMethodExit(typeResolver, methodInfo, result, thrown, callId);

        CallRecordLog recordLog = threadLocalRecordsLog.get();
        if (recordLog != null && recordLog.isComplete()) {
            threadLocalRecordsLog.clear();
            currentRecordingSessionCount.decrementAndGet();

            if (recordLog.size() >= context.getSysPropsSettings().getMinRecordsCountForLog()) {
                context.getTransport().uploadAsync(
                        new CallRecordTreeRequest(
                                recordLog,
                                MethodDescriptionMap.getInstance().values(),
                                typeResolver.getAllKnownTypes(),
                                context.getProcessInfo()
                        )
                );
            }
        }
    }

    public void endRecordingIfPossibleOnConstructorExit(TypeResolver typeResolver, MethodInfo methodInfo, long callId, Object result) {
        onConstructorExit(typeResolver, methodInfo, result, callId);

        CallRecordLog recordLog = threadLocalRecordsLog.get();
        if (recordLog != null && recordLog.isComplete()) {
            threadLocalRecordsLog.clear();
            currentRecordingSessionCount.decrementAndGet();

            if (recordLog.size() >= context.getSysPropsSettings().getMinRecordsCountForLog()) {
                context.getTransport().uploadAsync(
                        new CallRecordTreeRequest(
                                recordLog,
                                MethodDescriptionMap.getInstance().values(),
                                typeResolver.getAllKnownTypes(),
                                context.getProcessInfo()
                        )
                );
            }
        }
    }

    public long onConstructorEnter(MethodInfo method, Object[] args) {
        return onMethodEnter(method, null, args);
    }

    public long onMethodEnter(MethodInfo method, @Nullable Object callee, Object[] args) {
        CallRecordLog callRecordLog = threadLocalRecordsLog.get();
        if (callRecordLog == null) {
            return -1;
        }
        return callRecordLog.onMethodEnter(method.getId(), method.getParamPrinters(), callee, args);
    }

    public void onConstructorExit(TypeResolver typeResolver, MethodInfo method, Object result, long callId) {
        onMethodExit(typeResolver, method, result,null, callId);
    }

    public void onMethodExit(TypeResolver typeResolver, MethodInfo method, Object result, Throwable thrown, long callId) {
        CallRecordLog currentRecordLog = threadLocalRecordsLog.get();
        if (currentRecordLog == null) return;
        currentRecordLog.onMethodExit(method.getId(), method.getResultPrinter(), result, thrown, callId);

        if (currentRecordLog.estimateBytesSize() > 64 * 1024 * 1024/* ||
                (System.currentTimeMillis() - currentRecordLog.getEpochMillisCreatedTime()) > 1000*/) {
            CallRecordLog newRecordLog = currentRecordLog.cloneWithoutData();
            threadLocalRecordsLog.set(newRecordLog);

            context.getTransport().uploadAsync(
                    new CallRecordTreeRequest(
                            currentRecordLog,
                            MethodDescriptionMap.getInstance().values(),
                            typeResolver.getAllKnownTypes(),
                            context.getProcessInfo()
                    )
            );
        }
    }
}
