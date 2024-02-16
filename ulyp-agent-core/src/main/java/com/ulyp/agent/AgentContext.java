package com.ulyp.agent;

import com.ulyp.agent.bootstrap.RecordingDataWriterFactory;
import com.ulyp.agent.policy.*;
import com.ulyp.agent.util.MetricDumper;
import com.ulyp.core.MethodRepository;
import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.metrics.Metrics;
import com.ulyp.core.metrics.MetricsImpl;
import com.ulyp.core.metrics.NullMetrics;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.storage.writer.RecordingDataWriter;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.regex.Pattern;

public class AgentContext {

    private static volatile AgentContext instance;

    private static volatile boolean agentLoaded = false;

    private final Settings settings;
    private final OverridableRecordingPolicy startRecordingPolicy;
    private final RecordingDataWriter recordingDataWriter;
    private final ProcessMetadata processMetadata;
    private final TypeResolver typeResolver;
    private final MethodRepository methodRepository;
    @Nullable
    private final AutoCloseable apiServer;
    @Getter
    private final Metrics metrics;
    @Nullable
    private final MetricDumper metricDumper;

    private AgentContext() {
        this.settings = Settings.fromSystemProperties();
        if (settings.isMetricsEnabled()) {
            this.metrics = new MetricsImpl();
            this.metricDumper = new MetricDumper(metrics);
        } else {
            this.metrics = new NullMetrics();
            this.metricDumper = null;
        }
        this.startRecordingPolicy = initializePolicy(settings);
        this.recordingDataWriter = new RecordingDataWriterFactory().build(settings.getRecordingDataFilePath(), metrics);
        this.methodRepository = new MethodRepository();
        this.processMetadata = ProcessMetadata.builder()
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
        instance = new AgentContext();
        agentLoaded = true;
    }

    public MethodRepository getMethodRepository() {
        return methodRepository;
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
