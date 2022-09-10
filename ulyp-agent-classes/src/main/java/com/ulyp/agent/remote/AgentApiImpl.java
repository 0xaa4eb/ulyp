package com.ulyp.agent.remote;

import com.ulyp.agent.api.AgentApiGrpc;
import com.ulyp.agent.api.RecordingEnabled;
import com.ulyp.agent.api.RecordingEnabledResponse;
import com.ulyp.agent.policy.StartRecordingPolicy;
import io.grpc.stub.StreamObserver;

public class AgentApiImpl extends AgentApiGrpc.AgentApiImplBase {

    private final StartRecordingPolicy policy;

    public AgentApiImpl(StartRecordingPolicy policy) {
        this.policy = policy;
    }

    @Override
    public void setRecording(RecordingEnabled request, StreamObserver<RecordingEnabledResponse> responseObserver) {
        try {

            policy.forceEnableRecording(request.getValue());
            responseObserver.onNext(RecordingEnabledResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception err) {
            responseObserver.onError(err);
        }
    }
}
