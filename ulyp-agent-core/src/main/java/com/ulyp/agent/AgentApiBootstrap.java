package com.ulyp.agent;

import com.ulyp.core.MethodRepository;
import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.exception.AgentConfigurationException;
import com.ulyp.storage.writer.RecordingDataWriter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

/**
 * Bootstraps GRPC api for the agent using reflection. This is done since agent might be built without api included.
 */
@Slf4j
public class AgentApiBootstrap {

    public static AutoCloseable bootstrap(
            Consumer<Boolean> setRecordingEnabled,
            MethodRepository methodRepository,
            TypeResolver typeResolver,
            RecordingDataWriter recordingDataWriter,
            ProcessMetadata processMetadata,
            int listenPort) {
        try {
            Class<?> apiImplClass = Class.forName("com.ulyp.agent.AgentApiImpl");
            Constructor<?> apiImplConstructor = apiImplClass.getDeclaredConstructors()[0];
            Object apiImpl = apiImplConstructor.newInstance(
                    setRecordingEnabled,
                    methodRepository,
                    typeResolver,
                    recordingDataWriter,
                    processMetadata
            );

            Class<?> grpcServerClass = Class.forName("com.ulyp.agent.AgentApiGrpcServer");
            Constructor<?> grpcServerConstructor = grpcServerClass.getDeclaredConstructors()[0];
            Object apiServer = grpcServerConstructor.newInstance(
                    listenPort,
                    apiImpl
            );
            return (AutoCloseable) apiServer;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            throw new AgentConfigurationException("Could not initialize agent api, please verify the agent is built with '-PapiBuild=enabled'", exception);
        }
    }
}
