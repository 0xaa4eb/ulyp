package com.ulyp.agent;

public class AgentInitializationException extends RuntimeException {

    public AgentInitializationException(String message) {
        super(message);
    }

    public AgentInitializationException(Throwable cause) {
        super(cause);
    }

    public AgentInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
