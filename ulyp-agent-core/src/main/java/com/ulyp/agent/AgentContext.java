package com.ulyp.agent;

import com.ulyp.agent.bootstrap.RecordingDataWriterFactory;
import com.ulyp.agent.policy.*;
import com.ulyp.agent.queue.RecordingQueue;
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

import lombok.Getter;

public class AgentContext {

    @Getter
    private static volatile AgentContext ctx;
    private static volatile boolean agentLoaded = false;

    @Getter
    private final Settings settings;
    private final OverridableRecordingPolicy startRecordingPolicy;
    private final RecordingDataWriter recordingDataWriter;
    @Getter
    private final ProcessMetadata processMetadata;
    @Getter
    private final TypeResolver typeResolver;
    @Getter
    private final MethodRepository methodRepository;
    @Getter
    private final RecordingQueue recordingQueue;
    @Getter
    private final Recorder recorder;
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

        // TODO move string checks to policy factory
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

    public static boolean isLoaded() {
        return agentLoaded;
    }

    public RecordingDataWriter getStorageWriter() {
        return recordingDataWriter;
    }
}
