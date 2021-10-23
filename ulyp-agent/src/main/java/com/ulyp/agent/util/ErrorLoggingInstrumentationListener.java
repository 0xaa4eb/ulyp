package com.ulyp.agent.util;

import com.ulyp.core.log.AgentLogManager;
import com.ulyp.core.log.Logger;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

public class ErrorLoggingInstrumentationListener implements AgentBuilder.Listener {

    private static final Logger LOGGER = AgentLogManager.getLogger(ErrorLoggingInstrumentationListener.class);

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
        LOGGER.error("Failed to instrument " + typeName, throwable);
    }

    @Override
    public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {

    }
}
