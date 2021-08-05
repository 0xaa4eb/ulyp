package com.ulyp.core.log;

public class AgentLogManager {

    public static Logger getLogger(final Class<?> clazz) {
        if (!LoggingSettings.IS_LOGGING_TURNED_ON) {
            return new EmptyLogger();
        } else {
            return new SysOutLogger();
        }
    }
}
