package com.ulyp.agent;

import com.ulyp.agent.util.EnhancedThreadLocal;
import com.ulyp.agent.policy.StartRecordingPolicy;
import com.ulyp.core.*;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.util.LoggingSettings;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
@Slf4j
@ThreadSafe
public class Recorder {

    /**
     * Keeps current active recording session count. Based on the fact that most of the time there is no
     * recording sessions and this counter is equal to 0, it's possible to make a small performance optimization.
     * Advice code (see com.ulyp.agent.MethodCallRecordingAdvice) can first check if there are any recording sessions are active at all.
     * If there are any, then advice code will check thread local and know if there is recording session in this thread precisely.
     * This helps minimizing unneeded thread local lookups in the advice code
     */

    public static final AtomicInteger currentRecordingSessionCount = new AtomicInteger();
    private static final Recorder instance = new Recorder(AgentContext.getInstance());

    private final EnhancedThreadLocal<CallRecordLog> threadLocalRecordsLog = new EnhancedThreadLocal<>();
    private final CallIdGenerator initialCallIdGenerator;
    private final StartRecordingPolicy startRecordingPolicy;
    private final AgentContext context;

    public Recorder(AgentContext context) {
        this.context = context;
        this.startRecordingPolicy = context.getStartRecordingPolicy();
        this.initialCallIdGenerator = context.getCallIdGenerator();
    }

    public static Recorder getInstance() {
        return instance;
    }

    public boolean recordingIsActiveInCurrentThread() {
        return threadLocalRecordsLog.get() != null;
    }

    public long startOrContinueRecordingOnMethodEnter(TypeResolver typeResolver, Method method, @Nullable Object callee, Object[] args) {
        if (startRecordingPolicy.canStartRecording()) {
            threadLocalRecordsLog.computeIfAbsent(() -> {
                CallRecordLog callRecordLog = new CallRecordLog(typeResolver, initialCallIdGenerator.getNextStartValue());
                currentRecordingSessionCount.incrementAndGet();
                if (LoggingSettings.INFO_ENABLED) {
                    log.info("Started recording {} at method {}", callRecordLog.getRecordingMetadata().getId(), method.toShortString());
                }
                return callRecordLog;
            });
        }

        return onMethodEnter(method, callee, args);
    }

    public long startOrContinueRecordingOnConstructorEnter(TypeResolver typeResolver, Method method, Object[] args) {
        if (startRecordingPolicy.canStartRecording()) {
            threadLocalRecordsLog.computeIfAbsent(() -> {
                CallRecordLog callRecordLog = new CallRecordLog(typeResolver, initialCallIdGenerator.getNextStartValue());
                currentRecordingSessionCount.incrementAndGet();
                if (LoggingSettings.INFO_ENABLED) {
                    log.info("Started recording {} at method {}", callRecordLog.getRecordingMetadata().getId(), method.toShortString());
                }
                return callRecordLog;
            });
        }

        return onConstructorEnter(method, args);
    }

    private void write(TypeResolver typeResolver, CallRecordLog recordLog) {

        MethodList methods = new MethodList();
        for (Method method : MethodRepository.getInstance().values()) {
            if (!method.wasWrittenToFile()) {
                methods.add(method);
                method.markWrittenToFile();
                if (LoggingSettings.DEBUG_ENABLED) {
                    log.debug("Will write {} to storage", method);
                }
            }
        }
        context.getStorageWriter().write(methods);

        TypeList types = new TypeList();
        for (Type type : typeResolver.getAllResolved()) {
            if (!type.wasWrittenToFile()) {
                types.add(type);
                type.setWrittenToFile();
                if (LoggingSettings.DEBUG_ENABLED) {
                    log.debug("Will write {} to storage", type);
                }
            }
        }
        context.getStorageWriter().write(types);

        context.getStorageWriter().write(recordLog.getRecordingMetadata());
        context.getStorageWriter().write(recordLog.getRecordedCalls());
    }

    public long onConstructorEnter(Method method, Object[] args) {
        return onMethodEnter(method, null, args);
    }

    public long onMethodEnter(Method method, @Nullable Object callee, Object[] args) {
        try {
            CallRecordLog callRecordLog = threadLocalRecordsLog.get();
            if (callRecordLog == null) {
                return -1;
            }
            return callRecordLog.onMethodEnter(method, callee, args);
        } catch (Throwable err) {
            log.error("Error happened when recording", err);
            return -1;
        }
    }

    public void onConstructorExit(TypeResolver typeResolver, Method method, Object result, long callId) {
        onMethodExit(typeResolver, method, result, null, callId);
    }

    public void onMethodExit(TypeResolver typeResolver, Method method, Object result, Throwable thrown, long callId) {
        try {
            CallRecordLog currentRecordLog = threadLocalRecordsLog.get();
            if (currentRecordLog == null) return;
            currentRecordLog.onMethodExit(method, result, thrown, callId);

            if (currentRecordLog.isComplete() ||
                    currentRecordLog.estimateBytesSize() > 32 * 1024 * 1024 ||
                    (
                            (System.currentTimeMillis() - currentRecordLog.getRecordingMetadata().getLogCreatedEpochMillis()) > 100
                                    &&
                                    currentRecordLog.size() > 0
                    )) {
                CallRecordLog newRecordLog = currentRecordLog.cloneWithoutData();

                if (!currentRecordLog.isComplete()) {
                    threadLocalRecordsLog.set(newRecordLog);
                } else {
                    threadLocalRecordsLog.clear();
                    currentRecordingSessionCount.decrementAndGet();
                    if (LoggingSettings.INFO_ENABLED) {
                        log.info("Finished recording {} at method {}, recorded {} calls", currentRecordLog.getRecordingMetadata().getId(), method.toShortString(), currentRecordLog.size());
                    }
                }

                write(typeResolver, currentRecordLog);
            }
        } catch (Throwable err) {
            log.error("Error happened when recording", err);
        }
    }
}
