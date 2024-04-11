package com.ulyp.agent.advice;

import com.ulyp.agent.MethodId;
import com.ulyp.agent.MethodIdFactory;
import com.ulyp.agent.RecorderInstance;

import net.bytebuddy.asm.Advice;

/**
 * Advice which instructs how to instrument constructors. The byte buddy library copies the bytecode of methods into
 * constructors being instrumented.
 */
public class StartRecordingConstructorAdvice {

    /**
     * @param methodId injected right into bytecode unique method id. Mapping is made by
     *                 {@link MethodIdFactory} class.
     */
    @SuppressWarnings("UnusedAssignment")
    @Advice.OnMethodEnter
    static void enter(
            @Advice.Local("callId") int callId,
            @MethodId int methodId,
            @Advice.AllArguments Object[] arguments) {
        callId = RecorderInstance.instance.startOrContinueRecordingOnConstructorEnter(methodId, arguments);
    }

    /**
     * @param methodId injected right into bytecode unique method id. Mapping is made by
     *                 {@link MethodIdFactory} class. Guaranteed to be the same
     *                 as for enter advice
     */
    @Advice.OnMethodExit
    static void exit(
            @Advice.Local("callId") int callId,
            @MethodId int methodId,
            @Advice.This Object returnValue) {
        if (callId > 0) {
            RecorderInstance.instance.onConstructorExit(methodId, returnValue, callId);
        }
    }
}
