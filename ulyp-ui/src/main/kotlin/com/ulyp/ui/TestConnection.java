package com.ulyp.ui;

import com.ulyp.agent.api.AgentApiGrpc;
import com.ulyp.agent.api.RecordingEnabled;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

public class TestConnection {

    public static void main(String[] args) throws InterruptedException {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9080)
                .usePlaintext()
                .build();

        AgentApiGrpc.AgentApiBlockingStub stub = AgentApiGrpc.newBlockingStub(channel);

        stub.setRecording(RecordingEnabled.newBuilder().setValue(true).build());

        Thread.sleep(5000L);

        channel.shutdownNow();
        channel.awaitTermination(1, TimeUnit.MINUTES);
    }
}
