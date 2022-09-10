package com.ulyp.agent.remote;

import io.grpc.netty.NettyServerBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class AgentApiGrpcServer implements AutoCloseable {

    public AgentApiGrpcServer(int port, AgentApiImpl agentApi) {
        // TODO executor?
        try {
            NettyServerBuilder.forPort(port).addService(agentApi).build().start();
            log.info("Started API server at port {}", port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {

    }
}
