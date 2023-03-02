package com.ulyp.agent.remote;

import com.ulyp.agent.AgentContext;
import com.ulyp.agent.api.*;
import com.ulyp.core.Method;
import com.ulyp.core.MethodRepository;
import com.ulyp.core.Type;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.storage.ResetMetadata;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

@Slf4j
public class AgentApiImpl extends AgentApiGrpc.AgentApiImplBase {

    private final AgentContext agentContext;

    public AgentApiImpl(AgentContext agentContext) {
        this.agentContext = agentContext;
    }

    @Override
    public void setRecording(RecordingEnabled request, StreamObserver<RecordingEnabledResponse> responseObserver) {
        try {

            agentContext.getStartRecordingPolicy().forceEnableRecording(request.getValue());

            responseObserver.onNext(RecordingEnabledResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception err) {
            responseObserver.onError(err);
        }
    }

    @Override
    public void resetRecordingFile(ResetRecordingFileRequest request, StreamObserver<ResetRecordingFileResponse> responseObserver) {
        try {

            MethodList methods = new MethodList();
            for (Method method : agentContext.getMethodRepository().values()) {
                methods.add(method);
                method.markWrittenToFile();
                if (LoggingSettings.DEBUG_ENABLED) {
                    log.debug("Will write {} to storage", method);
                }
            }

            TypeList types = new TypeList();
            for (Type type : agentContext.getTypeResolver().getAllResolved()) {
                types.add(type);
                type.setWrittenToFile();
                if (LoggingSettings.DEBUG_ENABLED) {
                    log.debug("Will write {} to storage", type);
                }
            }

            agentContext.getStorageWriter().reset(
                    ResetMetadata.builder()
                            .methods(methods)
                            .types(types)
                            .processMetadata(agentContext.getProcessMetadata())
                            .build()
            );

            responseObserver.onNext(ResetRecordingFileResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception err) {
            responseObserver.onError(err);
        }
    }
}
