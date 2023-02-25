package com.ulyp.agent;

import com.ulyp.agent.util.EnhancedThreadLocal;
import com.ulyp.agent.policy.StartRecordingPolicy;
import com.ulyp.core.*;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.util.ConcurrentArrayList;
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

    private final EnhancedThreadLocal<CallRecordBuffer> threadLocalRecordsLog = new EnhancedThreadLocal<>();
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

    public CallRecordBuffer getCurrentCallRecordLog() {
        return threadLocalRecordsLog.get();
    }

    public long startOrContinueRecordingOnMethodEnter(TypeResolver typeResolver, Method method, @Nullable Object callee, Object[] args) {
        if (startRecordingPolicy.canStartRecording()) {
            CallRecordBuffer callRecordBuffer = threadLocalRecordsLog.computeIfAbsent(() -> {
                CallRecordBuffer newCallRecordBuffer = new CallRecordBuffer(typeResolver, initialCallIdGenerator.getNextStartValue());
                currentRecordingSessionCount.incrementAndGet();
                if (LoggingSettings.INFO_ENABLED) {
                    log.info("Started recording {} at method {}", newCallRecordBuffer.getRecordingMetadata().getId(), method.toShortString());
                }
                return newCallRecordBuffer;
            });

            return onMethodEnter(callRecordBuffer, method, callee, args);
        } else {
            return -1;
        }
    }

    public long startOrContinueRecordingOnConstructorEnter(TypeResolver typeResolver, Method method, Object[] args) {
        if (startRecordingPolicy.canStartRecording()) {
            CallRecordBuffer callRecordBuffer = threadLocalRecordsLog.computeIfAbsent(() -> {
                CallRecordBuffer newCallRecordBuffer = new CallRecordBuffer(typeResolver, initialCallIdGenerator.getNextStartValue());
                currentRecordingSessionCount.incrementAndGet();
                if (LoggingSettings.INFO_ENABLED) {
                    log.info("Started recording {} at method {}", newCallRecordBuffer.getRecordingMetadata().getId(), method.toShortString());
                }
                return newCallRecordBuffer;
            });

            return onConstructorEnter(callRecordBuffer, method, args);
        } else {
            return -1;
        }
    }

    private final AtomicInteger lastIndexOfMethodWritten = new AtomicInteger(-1);
    private final AtomicInteger lastIndexOfMethodToRecordWritten = new AtomicInteger(-1);
    private final AtomicInteger lastIndexOfTypeWritten = new AtomicInteger(-1);

    private void write(TypeResolver typeResolver, CallRecordBuffer recordLog) {

        MethodList methodsList = new MethodList();

        ConcurrentArrayList<Method> methods = MethodRepository.getInstance().getMethods();
        int upToExcluding = methods.size() - 1;
        int startFrom = lastIndexOfMethodWritten.get() + 1;

        for (int i = startFrom; i <= upToExcluding; i++) {
            Method method = methods.get(i);
            log.debug("Will write {} to storage", method);
            methodsList.add(method);
        }
        if (methodsList.size() > 0) {
            context.getStorageWriter().write(methodsList);
            for (;;) {
                int currentIndex = lastIndexOfMethodWritten.get();
                if (currentIndex < upToExcluding) {
                    if (lastIndexOfMethodWritten.compareAndSet(currentIndex, upToExcluding)) {
                        break;
                    }
                } else {
                    // Someone else must have written methods already
                    break;
                }
            }
        }

        methodsList = new MethodList();
        methods = MethodRepository.getInstance().getRecordingStartMethods();
        upToExcluding = methods.size() - 1;
        startFrom = lastIndexOfMethodToRecordWritten.get() + 1;

        for (int i = startFrom; i <= upToExcluding; i++) {
            Method method = methods.get(i);
            log.debug("Will write {} to storage", method);
            methodsList.add(method);
        }
        if (methodsList.size() > 0) {
            context.getStorageWriter().write(methodsList);
            for (;;) {
                int currentIndex = lastIndexOfMethodToRecordWritten.get();
                if (currentIndex < upToExcluding) {
                    if (lastIndexOfMethodToRecordWritten.compareAndSet(currentIndex, upToExcluding)) {
                        break;
                    }
                } else {
                    // Someone else must have written methods already
                    break;
                }
            }
        }

        TypeList typesList = new TypeList();
        ConcurrentArrayList<Type> types = typeResolver.getAllResolvedAsConcurrentList();
        upToExcluding = types.size() - 1;
        startFrom = lastIndexOfTypeWritten.get() + 1;

        for (int i = startFrom; i <= upToExcluding; i++) {
            Type type = types.get(i);
            log.debug("Will write {} to storage", type);
            typesList.add(type);
        }
        if (typesList.size() > 0) {
            context.getStorageWriter().write(typesList);
            for (;;) {
                int currentIndex = lastIndexOfTypeWritten.get();
                if (currentIndex < upToExcluding) {
                    if (lastIndexOfTypeWritten.compareAndSet(currentIndex, upToExcluding)) {
                        break;
                    }
                } else {
                    // Someone else must have written methods already
                    break;
                }
            }
        }

        context.getStorageWriter().write(recordLog.getRecordingMetadata());
        context.getStorageWriter().write(recordLog.getRecordedCalls());
    }

    public long onConstructorEnter(Method method, Object[] args) {
        return onMethodEnter(threadLocalRecordsLog.get(), method, null, args);
    }

    public long onConstructorEnter(CallRecordBuffer callRecordBuffer, Method method, Object[] args) {
        return onMethodEnter(callRecordBuffer, method, null, args);
    }

    public long onMethodEnter(Method method, @Nullable Object callee, Object[] args) {
        return onMethodEnter(threadLocalRecordsLog.get(), method, callee, args);
    }

    public long onMethodEnter(CallRecordBuffer callRecordBuffer, Method method, @Nullable Object callee, Object[] args) {
        try {
            if (callRecordBuffer == null) {
                return -1;
            }
            return callRecordBuffer.onMethodEnter(method, callee, args);
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
            CallRecordBuffer currentRecordLog = threadLocalRecordsLog.get();
            if (currentRecordLog == null) return;
            currentRecordLog.onMethodExit(method, result, thrown, callId);

            if (currentRecordLog.isComplete() ||
                    currentRecordLog.estimateBytesSize() > 32 * 1024 * 1024 ||
                    (
                            (System.currentTimeMillis() - currentRecordLog.getRecordingMetadata().getLogCreatedEpochMillis()) > 100
                                    &&
                                    currentRecordLog.getRecordedCallsSize() > 0
                    )) {
                CallRecordBuffer newRecordLog = currentRecordLog.cloneWithoutData();

                if (!currentRecordLog.isComplete()) {
                    threadLocalRecordsLog.set(newRecordLog);
                } else {
                    threadLocalRecordsLog.clear();
                    currentRecordingSessionCount.decrementAndGet();
                    if (LoggingSettings.INFO_ENABLED) {
                        log.info("Finished recording {} at method {}, recorded {} calls", currentRecordLog.getRecordingMetadata().getId(), method.toShortString(), currentRecordLog.getTotalRecordedEnterCalls());
                    }
                }

                write(typeResolver, currentRecordLog);
            }
        } catch (Throwable err) {
            log.error("Error happened when recording", err);
        }
    }
}
