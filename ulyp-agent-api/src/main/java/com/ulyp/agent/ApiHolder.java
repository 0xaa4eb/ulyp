package com.ulyp.agent;

import com.ulyp.agent.api.AgentApiGrpc;

public class ApiHolder {

    static volatile AgentApiGrpc.AgentApiImplBase instance;
}
