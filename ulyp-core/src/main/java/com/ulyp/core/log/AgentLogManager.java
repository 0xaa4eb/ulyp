package com.ulyp.core.log;

public class AgentLogManager {

    public static Logger getLogger(final Class<?> clazz) {
        return new SysOutLogger();
    }
}
