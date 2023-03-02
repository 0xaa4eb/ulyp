package com.ulyp.agent;

import com.ulyp.agent.policy.*;
import com.ulyp.agent.remote.AgentApiImpl;
import com.ulyp.agent.remote.AgentApiGrpcServer;
import com.ulyp.core.MethodRepository;
import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.util.Classpath;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.storage.StorageWriter;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.time.Duration;

public class AgentContext {

    private static volatile AgentContext instance;

    private static volatile boolean agentLoaded = false;

    private final Settings settings;
    private final StartRecordingPolicy startRecordingPolicy;
    private final CallIdGenerator callIdGenerator;
    private final StorageWriter storageWriter;
    private final ProcessMetadata processMetadata;
    private final TypeResolver typeResolver;
    private final MethodRepository methodRepository;
    @Nullable
    private final AgentApiGrpcServer apiServer;

    private AgentContext() {
        this.callIdGenerator = new CallIdGenerator();
        this.settings = Settings.fromSystemProperties();
        this.startRecordingPolicy = initializePolicy(settings.getStartRecordingPolicyPropertyValue());
        this.storageWriter = settings.buildStorageWriter();
        this.methodRepository = new MethodRepository();
        this.processMetadata = ProcessMetadata.builder()
                .classPathFiles(new Classpath().toList())
                .mainClassName(ProcessMetadata.getMainClassNameFromProp())
                .pid(System.currentTimeMillis())
                .build();
        this.typeResolver = ReflectionBasedTypeResolver.getInstance();

        if (!settings.isAgentDisabled()) {
            this.storageWriter.write(processMetadata);

            Thread shutdown = new Thread(storageWriter::close);
            Runtime.getRuntime().addShutdownHook(shutdown);
        }
        if (settings.getBindNetworkAddress() != null) {
            apiServer = new AgentApiGrpcServer(Integer.parseInt(settings.getBindNetworkAddress()), new AgentApiImpl(this));
        } else {
            apiServer = null;
        }
    }

    private static StartRecordingPolicy initializePolicy(String value) {
        if (value == null || value.isEmpty()) {
            return new EnabledByDefaultRecordingPolicy();
        }
        // TODO move string checks to policy implementations
        if (value.startsWith("delay:")) {
            return new DelayBasedRecordingPolicy(Duration.ofSeconds(Integer.parseInt(value.replace("delay:", ""))));
        }
        if (value.equals("api")) {
            return new DisabledByDefaultRecordingPolicy();
        }
        if (value.startsWith("file:")) {
            return new FileBasedStartRecordingPolicy(Paths.get(value.replace("file:", "")));
        }
        throw new IllegalArgumentException("Unsupported recording policy: " + value);
    }

    public static void init() {
        instance = new AgentContext();
        agentLoaded = true;
    }

    public MethodRepository getMethodRepository() {
        return methodRepository;
    }

    public ProcessMetadata getProcessMetadata() {
        return processMetadata;
    }

    public TypeResolver getTypeResolver() {
        return typeResolver;
    }

    public static boolean isLoaded() {
        return agentLoaded;
    }

    public StartRecordingPolicy getStartRecordingPolicy() {
        return startRecordingPolicy;
    }

    public static AgentContext getInstance() {
        return instance;
    }

    public StorageWriter getStorageWriter() {
        return storageWriter;
    }

    public CallIdGenerator getCallIdGenerator() {
        return callIdGenerator;
    }
}
