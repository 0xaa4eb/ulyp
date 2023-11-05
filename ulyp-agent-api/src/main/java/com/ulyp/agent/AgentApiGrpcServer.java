package com.ulyp.agent;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AgentApiGrpcServer implements AutoCloseable {

    private final Server server;

    public AgentApiGrpcServer(int port, AgentApiImpl agentApi) {
        try {
            server = NettyServerBuilder.forPort(port).addService(agentApi).build().start();
            log.info("Started API server at port {}", port);
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    @Override
    public void close() throws Exception {
        server.shutdownNow();
        server.awaitTermination(1, TimeUnit.MINUTES);
    }
}
