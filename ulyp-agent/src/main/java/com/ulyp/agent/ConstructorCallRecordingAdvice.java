package com.ulyp.agent;

import com.ulyp.core.MethodRepository;

import net.bytebuddy.asm.Advice;

/**
 * Advice which instructs how to instrument constructors. The byte buddy library copies the bytecode of methods into
 * constructors being instrumented.
 */
public class ConstructorCallRecordingAdvice {

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

        // This if check is ugly, but the code is wired into bytecode, so it's more efficient to check right away instead of calling a method
        if (methodId >= MethodRepository.RECORD_METHODS_MIN_ID) {
            callToken = RecorderInstance.instance.startRecordingOnMethodEnter(methodId, null, arguments);
        } else {
            if (Recorder.currentRecordingSessionCount.get() > 0) {
                RecordingState recordingState = RecorderInstance.instance.getCurrentRecordingState();
                if (recordingState != null) {
                    //noinspection UnusedAssignment
                    callToken = RecorderInstance.instance.onMethodEnter(recordingState, methodId, null, arguments);
                }
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
