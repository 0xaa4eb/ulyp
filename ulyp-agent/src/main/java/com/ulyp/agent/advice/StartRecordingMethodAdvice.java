package com.ulyp.agent.advice;

import com.ulyp.agent.MethodId;
import com.ulyp.agent.MethodIdFactory;
import com.ulyp.agent.RecorderInstance;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

/**
 * Advice which instructs how to instrument methods. The byte buddy library copies the bytecode of methods into
 * constructors being instrumented.
 */
public class StartRecordingMethodAdvice {

    /**
     * @param methodId injected right into bytecode unique method id. Mapping is made by
     *                 {@link MethodIdFactory} class.
     */
    @Advice.OnMethodEnter
    static void enter(
            @MethodId int methodId,
            @Advice.Local("callToken") long callToken,
            @Advice.This(optional = true) Object callee,
            @Advice.AllArguments Object[] arguments) {

        // noinspection UnusedAssignment local variable callId is used by exit() method
        callToken = RecorderInstance.instance.startRecordingOnMethodEnter(methodId, callee, arguments);
    }

    /**
     * @param methodId injected right into bytecode unique method id. Mapping is made by
     *                 {@link MethodIdFactory} class. Guaranteed to be the same
     *                 as for enter advice
     */
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    static void exit(
            @MethodId int methodId,
            @Advice.Local("callToken") long callToken,
            @Advice.Thrown Throwable throwable,
            @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object returnValue) {
        if (callToken > 0) {
            RecorderInstance.instance.onMethodExit(methodId, returnValue, throwable, callToken);
        }
    }
}
