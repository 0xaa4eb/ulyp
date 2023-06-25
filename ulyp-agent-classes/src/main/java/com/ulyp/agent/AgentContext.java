package com.ulyp.agent;

import com.ulyp.agent.policy.*;
import com.ulyp.agent.remote.AgentApiImpl;
import com.ulyp.agent.remote.AgentApiGrpcServer;
import com.ulyp.core.MethodRepository;
import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.util.Classpath;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.storage.RecordingDataWriter;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.regex.Pattern;

public class AgentContext {

    private static volatile AgentContext instance;

    private static volatile boolean agentLoaded = false;

    private final Settings settings;
    private final StartRecordingPolicy startRecordingPolicy;
    private final RecordingDataWriter recordingDataWriter;
    private final ProcessMetadata processMetadata;
    private final TypeResolver typeResolver;
    private final MethodRepository methodRepository;
    @Nullable
    private final AgentApiGrpcServer apiServer;

    private AgentContext() {
        this.settings = Settings.fromSystemProperties();
        this.startRecordingPolicy = initializePolicy(settings);
        this.recordingDataWriter = settings.buildStorageWriter();
        this.methodRepository = new MethodRepository();
        this.processMetadata = ProcessMetadata.builder()
                .classPathFiles(new Classpath().toList())
                .mainClassName(ProcessMetadata.getMainClassNameFromProp())
                .pid(System.currentTimeMillis())
                .build();
        this.typeResolver = ReflectionBasedTypeResolver.getInstance();

        if (!settings.isAgentDisabled()) {
            this.recordingDataWriter.write(processMetadata);

            Thread shutdown = new Thread(recordingDataWriter::close);
            Runtime.getRuntime().addShutdownHook(shutdown);
        }
        if (settings.getBindNetworkAddress() != null) {
            apiServer = new AgentApiGrpcServer(Integer.parseInt(settings.getBindNetworkAddress()), new AgentApiImpl(this));
        } else {
            apiServer = null;
        }
    }

    private static StartRecordingPolicy initializePolicy(Settings settings) {
        String value = settings.getStartRecordingPolicyPropertyValue();

        StartRecordingPolicy policy;
        if (value == null || value.isEmpty()) {
            policy = new EnabledByDefaultRecordingPolicy();
        } else if (value.startsWith("delay:")) {
            // TODO move string checks to policy implementations
            policy = new DelayBasedRecordingPolicy(Duration.ofSeconds(Integer.parseInt(value.replace("delay:", ""))));
        } else if (value.equals("api")) {
            policy = new DisabledByDefaultRecordingPolicy();
        } else if (value.startsWith("file:")) {
            policy = new FileBasedStartRecordingPolicy(Paths.get(value.replace("file:", "")));
        } else {
            throw new IllegalArgumentException("Unsupported recording policy: " + value);
        }

        Pattern startRecordingThreads = settings.getStartRecordingThreads();
        if (startRecordingThreads != null) {
            return new ThreadNameRecordingPolicy(policy, startRecordingThreads);
        } else {
            return policy;
        }
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

    public RecordingDataWriter getStorageWriter() {
        return recordingDataWriter;
    }
}
