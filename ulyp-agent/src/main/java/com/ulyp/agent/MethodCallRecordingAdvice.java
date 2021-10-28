package com.ulyp.agent;

import com.ulyp.agent.util.ByteBuddyTypeResolver;
import com.ulyp.core.MethodStore;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

public class MethodCallRecordingAdvice {

    /**
     * @param methodId injected right into bytecode unique method id. Mapping is made by
     *                 {@link MethodIdFactory} class.
     */
    @Advice.OnMethodEnter
    static void enter(
            @MethodId int methodId,
            @Advice.Local("callId") long callId,
            @Advice.This(optional = true) Object callee,
            @Advice.AllArguments Object[] arguments) {
        if (methodId < 0) {
            callId = Recorder.getInstance().startOrContinueRecordingOnMethodEnter(
                    ByteBuddyTypeResolver.getInstance(),
                    MethodStore.getInstance().get(methodId),
                    callee,
                    arguments
            );
        } else {
            if (Recorder.currentRecordingSessionCount.get() > 0 && Recorder.getInstance().recordingIsActiveInCurrentThread()) {
                callId = Recorder.getInstance().onMethodEnter(MethodStore.getInstance().get(methodId), callee, arguments);
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
            @Advice.Local("callId") long callId,
            @Advice.Thrown Throwable throwable,
            @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object returnValue) {
        if (callId >= 0) {
            if (methodId < 0) {
                Recorder.getInstance().endRecordingIfPossibleOnMethodExit(
                        ByteBuddyTypeResolver.getInstance(),
                        MethodStore.getInstance().get(methodId),
                        returnValue,
                        throwable,
                        callId
                );
            } else {
                if (Recorder.currentRecordingSessionCount.get() > 0 && Recorder.getInstance().recordingIsActiveInCurrentThread()) {
                    Recorder.getInstance().onMethodExit(
                            ByteBuddyTypeResolver.getInstance(),
                            MethodStore.getInstance().get(methodId),
                            returnValue,
                            throwable,
                            callId
                    );
                }
            }
        }
    }
}
