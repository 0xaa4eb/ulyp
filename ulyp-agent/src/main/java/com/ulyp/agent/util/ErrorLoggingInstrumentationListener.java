package com.ulyp.agent.util;

import com.ulyp.core.util.LoggingSettings;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

@Slf4j
public class ErrorLoggingInstrumentationListener implements AgentBuilder.Listener {

    @Override
    public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {

    }

    @Override
    public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType) {

    }

    @Override
    public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {

    }

    @Override
    public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
        if (LoggingSettings.DEBUG_ENABLED) {
            log.error("Failed to instrument class " + typeName, throwable);
        }
    }

    @Override
    public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {

    }
}
