package com.ulyp.agent;

import com.ulyp.agent.api.*;
import com.ulyp.core.*;
import com.ulyp.core.mem.SerializedMethodList;
import com.ulyp.core.mem.SerializedTypeList;
import com.ulyp.core.util.ConcurrentArrayList;
import com.ulyp.core.util.LoggingSettings;
import com.ulyp.storage.writer.RecordingDataWriter;
import com.ulyp.storage.writer.ResetRequest;
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
        ApiHolder.instance = this;
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
            SerializedMethodList methods = new SerializedMethodList();
            for (Method method : methodRepository.values()) {
                methods.add(method);
                if (LoggingSettings.DEBUG_ENABLED) {
                    log.debug("Will write {} to storage", method);
                }
            }

            SerializedTypeList types = new SerializedTypeList();
            ConcurrentArrayList<Type> resolvedTypes = typeResolver.values();
            int size = resolvedTypes.size();
            for (int i = 0; i < size; i++) {
                Type type = resolvedTypes.get(i);
                if (type != null) {
                    types.add(resolvedTypes.get(i));
                    if (LoggingSettings.DEBUG_ENABLED) {
                        log.debug("Will write {} to storage", type);
                    }
                }
            }

            recordingDataWriter.reset(ResetRequest.builder()
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
