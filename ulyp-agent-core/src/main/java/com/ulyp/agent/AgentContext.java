package com.ulyp.agent;

import com.ulyp.agent.bootstrap.RecordingDataWriterFactory;
import com.ulyp.agent.options.AgentOptions;
import com.ulyp.agent.policy.OverridableRecordingPolicy;
import com.ulyp.agent.queue.RecordingEventQueue;
import com.ulyp.agent.util.MetricDumper;
import com.ulyp.core.*;
import com.ulyp.core.metrics.Metrics;
import com.ulyp.core.metrics.MetricsImpl;
import com.ulyp.core.metrics.NullMetrics;
import com.ulyp.core.util.Classpath;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.storage.writer.RecordingDataWriter;
import lombok.Getter;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import org.jetbrains.annotations.Nullable;

public class AgentContext {

    @Getter
    private static volatile AgentContext ctx;
    private static volatile boolean agentLoaded = false;

    @Getter
    private final AgentOptions options;
    private final OverridableRecordingPolicy startRecordingPolicy;
    private final RecordingDataWriter recordingDataWriter;
    @Getter
    private final ProcessMetadata processMetadata;
    @Getter
    private final TypeResolver typeResolver;
    @Getter
    private final Converter<MethodDescription, Method> methodResolver;
    @Getter
    private final Converter<TypeDescription.Generic, Type> typeConverter;
    @Getter
    private final MethodRepository methodRepository;
    @Getter
    private final RecordingEventQueue recordingEventQueue;
    @Getter
    private final Recorder recorder;
    @Nullable
    private final AutoCloseable apiServer;
    @Getter
    private final Metrics metrics;
    @Nullable
    private final MetricDumper metricDumper;
    private final RecorderContext recorderContext;

    private AgentContext(AgentContextBootstrap bootstrap) {
        this.options = new AgentOptions();
        if (options.getMetricsEnabled().get()) {
            this.metrics = new MetricsImpl();
            this.metricDumper = new MetricDumper(metrics);
        } else {
            this.metrics = new NullMetrics();
            this.metricDumper = null;
        }
        this.recorderContext = new RecorderContext(options);
        this.recorderContext.init();
        this.startRecordingPolicy = options.getStartRecordingPolicy().get();
        this.recordingDataWriter = new RecordingDataWriterFactory().build(options.getRecordingDataFilePath().get(), metrics);
        this.methodRepository = new MethodRepository();
        this.processMetadata = ProcessMetadata.builder()
                .mainClassName(ProcessMetadata.getMainClassNameFromProp())
                .pid(System.currentTimeMillis())
                .classpath(new Classpath().toList())
                .build();
        this.typeConverter = bootstrap.getTypeConverter();
        this.methodResolver = bootstrap.getMethodConverter();
        this.typeResolver = new ReflectionBasedTypeResolver();
        this.recordingEventQueue = new RecordingEventQueue(typeResolver, new AgentDataWriter(recordingDataWriter, methodRepository), metrics);
        this.recorder = new Recorder(options, typeResolver, methodRepository, startRecordingPolicy, recordingEventQueue, metrics);

        if (options.getBindNetworkAddress() != null) {
            apiServer = AgentApiBootstrap.bootstrap(
                    startRecordingPolicy::setRecordingCanStart,
                    methodRepository,
                    typeResolver,
                    recordingDataWriter,
                    processMetadata,
                    Integer.parseInt(options.getBindNetworkAddress())
            );
        } else {
            apiServer = null;
        }
    }

    public static void init(AgentContextBootstrap bootstrap) {
        ctx = new AgentContext(bootstrap);

        if (ctx.getOptions().isAgentEnabled()) {
            ctx.getStorageWriter().write(ctx.getProcessMetadata());

            Thread shutdown = new Thread(new AgentShutdownHook());
            Runtime.getRuntime().addShutdownHook(shutdown);

            ctx.getRecordingEventQueue().start();
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
