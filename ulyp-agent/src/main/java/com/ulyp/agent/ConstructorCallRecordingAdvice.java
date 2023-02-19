package com.ulyp.agent;

import com.ulyp.agent.util.ByteBuddyTypeResolver;
import com.ulyp.core.CallRecordLog;
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
            @Advice.Local("callId") long callId,
            @MethodId int methodId,
            @Advice.AllArguments Object[] arguments) {

        // This if check is ugly, but the code is wired into bytecode, so it's more efficient to check right away instead of calling a method
        if (methodId >= MethodRepository.RECORD_METHODS_MIN_ID) {
            callId = Recorder.getInstance().startOrContinueRecordingOnConstructorEnter(
                    ByteBuddyTypeResolver.getInstance(),
                    MethodRepository.getInstance().get(methodId),
                    arguments
            );
        } else {
            if (Recorder.currentRecordingSessionCount.get() > 0 && Recorder.getInstance().recordingIsActiveInCurrentThread()) {
                callId = Recorder.getInstance().onConstructorEnter(MethodRepository.getInstance().get(methodId), arguments);
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
            @Advice.Local("callId") long callId,
            @MethodId int methodId,
            @Advice.This Object returnValue) {

        if (callId >= 0) {
            Recorder.getInstance().onConstructorExit(
                ByteBuddyTypeResolver.getInstance(),
                MethodRepository.getInstance().get(methodId),
                returnValue,
                callId
            );
        }
    }
}
