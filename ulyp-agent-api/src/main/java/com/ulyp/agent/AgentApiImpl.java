package com.ulyp.agent;

import com.ulyp.agent.api.AgentApiGrpc;
import com.ulyp.agent.api.RecordingEnabled;
import com.ulyp.agent.api.RecordingEnabledResponse;
import com.ulyp.agent.api.ResetRecordingFileRequest;
import com.ulyp.agent.api.ResetRecordingFileResponse;
import com.ulyp.core.*;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.storage.RecordingDataWriter;
import com.ulyp.storage.ResetMetadata;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class AgentApiImpl extends AgentApiGrpc.AgentApiImplBase {

    private final Consumer<Boolean> startRecordingRunnable;
    private final MethodRepository methodRepository;
    private final TypeResolver typeResolver;
    private final RecordingDataWriter recordingDataWriter;
    private final ProcessMetadata processMetadata;

    public AgentApiImpl(Consumer<Boolean> startRecordingRunnable, MethodRepository methodRepository, TypeResolver typeResolver, RecordingDataWriter recordingDataWriter, ProcessMetadata processMetadata) {
        this.startRecordingRunnable = startRecordingRunnable;
        this.methodRepository = methodRepository;
        this.typeResolver = typeResolver;
        this.recordingDataWriter = recordingDataWriter;
        this.processMetadata = processMetadata;
    }

    @Override
    public void setRecording(RecordingEnabled request, StreamObserver<RecordingEnabledResponse> responseObserver) {
        try {
            startRecordingRunnable.accept(request.getValue());

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
            for (Method method : methodRepository.values()) {
                methods.add(method);
                if (LoggingSettings.DEBUG_ENABLED) {
                    log.debug("Will write {} to storage", method);
                }
            }

            TypeList types = new TypeList();
            for (Type type : typeResolver.getAllResolved()) {
                types.add(type);
                if (LoggingSettings.DEBUG_ENABLED) {
                    log.debug("Will write {} to storage", type);
                }
            }

            recordingDataWriter.reset(ResetMetadata.builder()
                            .methods(methods)
                            .types(types)
                            .processMetadata(processMetadata)
                            .build()
            );

            responseObserver.onNext(ResetRecordingFileResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception err) {
            responseObserver.onError(err);
        }
    }
}
