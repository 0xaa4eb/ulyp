package com.ulyp.agent.advice;

import com.ulyp.agent.*;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

/**
 * Advice which instructs how to instrument methods. The byte buddy library copies the bytecode of methods into
 * constructors being instrumented.
 *
 * This advice is for methods which accept three parameters
 */
public class MethodAdviceThreeArgs {

    /**
     * @param methodId injected right into bytecode unique method id. Mapping is made by
     *                 {@link MethodIdFactory} class.
     */
    @Advice.OnMethodEnter
    static void enter(
            @MethodId int methodId,
            @Advice.Local("callToken") long callToken,
            @Advice.This(optional = true) Object callee,
            @Advice.Argument(0) Object arg1,
            @Advice.Argument(1) Object arg2,
            @Advice.Argument(2) Object arg3) {

        if (Recorder.currentRecordingSessionCount.get() > 0) {
            RecordingThreadLocalContext recordingCtx = RecorderInstance.instance.getCtx();
            if (recordingCtx != null) {
                //noinspection UnusedAssignment
                callToken = RecorderInstance.instance.onMethodEnter(recordingCtx, methodId, callee, arg1, arg2, arg3);
            }
        }
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
