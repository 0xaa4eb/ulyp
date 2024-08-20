package com.ulyp.agent.advice;

import com.ulyp.agent.*;

import net.bytebuddy.asm.Advice;

/**
 * Advice which instructs how to instrument constructors. The byte buddy library copies the bytecode of methods into
 * constructors being instrumented.
 */
public class ConstructorAdvice {

    /**
     * @param methodId injected right into bytecode unique method id. Mapping is made by
     *                 {@link MethodIdFactory} class.
     */
    @SuppressWarnings("UnusedAssignment")
    @Advice.OnMethodEnter
    static void enter(
            @Advice.Local("callToken") long callToken,
            @MethodId int methodId,
            @Advice.AllArguments Object[] arguments) {

        if (Recorder.currentRecordingSessionCount.get() > 0) {
            RecordingThreadLocalContext recordingCtx = RecorderInstance.instance.getCtx();
            if (recordingCtx != null) {
                //noinspection UnusedAssignment
                callToken = RecorderInstance.instance.onMethodEnter(recordingCtx, methodId, null, arguments);
            }
        }
    }

    /**
     * @param methodId injected right into bytecode unique method id. Mapping is made by
     *                 {@link MethodIdFactory} class. Guaranteed to be the same
     *                 as for enter advice
     */
    @Advice.OnMethodExit
    static void exit(
            @Advice.Local("callToken") long callToken,
            @MethodId int methodId,
            @Advice.This Object returnValue) {
        if (callToken > 0) {
            RecorderInstance.instance.onMethodExit(methodId, returnValue, null, callToken);
        }
    }
}
