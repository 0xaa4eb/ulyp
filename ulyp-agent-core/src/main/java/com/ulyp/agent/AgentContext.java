package com.ulyp.agent;

import com.ulyp.agent.policy.*;
import com.ulyp.agent.queue.RecordingQueue;
import com.ulyp.core.MethodRepository;
import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.storage.writer.RecordingDataWriter;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.regex.Pattern;

import lombok.Getter;

public class AgentContext {

    private static volatile AgentContext ctx;
    private static volatile boolean agentLoaded = false;

    @Getter
    private final Settings settings;
    private final OverridableRecordingPolicy startRecordingPolicy;
    private final RecordingDataWriter recordingDataWriter;
    private final ProcessMetadata processMetadata;
    private final TypeResolver typeResolver;
    private final MethodRepository methodRepository;
    private final RecordingQueue recordingQueue;
    private final Recorder recorder;
    @Nullable
    private final AutoCloseable apiServer;

    private AgentContext() {
        this.settings = Settings.fromSystemProperties();
        this.startRecordingPolicy = initializePolicy(settings);
        this.recordingDataWriter = settings.buildStorageWriter();
        this.methodRepository = new MethodRepository();
        this.processMetadata = ProcessMetadata.builder()
                .mainClassName(ProcessMetadata.getMainClassNameFromProp())
                .pid(System.currentTimeMillis())
                .build();
        this.typeResolver = ReflectionBasedTypeResolver.getInstance();
        this.recordingQueue = new RecordingQueue(typeResolver, new AgentDataWriter(recordingDataWriter, methodRepository));
        this.recorder = new Recorder(typeResolver, methodRepository, startRecordingPolicy, recordingQueue);

        if (settings.getBindNetworkAddress() != null) {
            apiServer = AgentApiBootstrap.bootstrap(
                    startRecordingPolicy::setRecordingCanStart,
                    methodRepository,
                    typeResolver,
                    recordingDataWriter,
                    processMetadata,
                    Integer.parseInt(settings.getBindNetworkAddress())
            );
        } else {
            apiServer = null;
        }
    }

    private static OverridableRecordingPolicy initializePolicy(Settings settings) {
        String value = settings.getStartRecordingPolicyPropertyValue();

        // TODO move string checks to policy implementations
        StartRecordingPolicy policy;
        if (value == null || value.isEmpty()) {
            policy = new EnabledRecordingPolicy();
        } else if (value.startsWith("delay:")) {
            policy = new DelayBasedRecordingPolicy(Duration.ofSeconds(Integer.parseInt(value.replace("delay:", ""))));
        } else if (value.equals("api")) {
            policy = new DisabledRecordingPolicy();
        } else if (value.startsWith("file:")) {
            policy = new FileBasedStartRecordingPolicy(Paths.get(value.replace("file:", "")));
        } else {
            throw new IllegalArgumentException("Unsupported recording policy: " + value);
        }

        Pattern startRecordingThreads = settings.getStartRecordingThreads();
        if (startRecordingThreads != null) {
            policy = new ThreadNameRecordingPolicy(policy, startRecordingThreads);
        }
        return new OverridableRecordingPolicy(policy);
    }

    public static void init() {
        ctx = new AgentContext();

        if (ctx.getSettings().isAgentEnabled()) {
            ctx.getStorageWriter().write(ctx.getProcessMetadata());

            Thread shutdown = new Thread(new AgentShutdownHook());
            Runtime.getRuntime().addShutdownHook(shutdown);

            ctx.getRecordingQueue().start();
        }

        agentLoaded = true;
    }

    public RecordingQueue getRecordingQueue() {
        return recordingQueue;
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

    public Recorder getRecorder() {
        return recorder;
    }

    public static AgentContext getCtx() {
        return ctx;
    }

    public RecordingDataWriter getStorageWriter() {
        return recordingDataWriter;
    }
}
