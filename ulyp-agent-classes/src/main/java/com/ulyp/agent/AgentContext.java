package com.ulyp.agent;

import com.ulyp.agent.util.AlwaysStartRecordingPolicy;
import com.ulyp.agent.util.DelayBasedRecordingPolicy;
import com.ulyp.agent.util.FileBasedStartRecordingPolicy;
import com.ulyp.agent.util.StartRecordingPolicy;
import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.util.Classpath;
import com.ulyp.storage.StorageWriter;

import java.nio.file.Paths;
import java.time.Duration;

public class AgentContext {

    private static final AgentContext instance = new AgentContext();

    private static volatile boolean agentLoaded = false;
    private final Settings settings;
    private final StartRecordingPolicy startRecordingPolicy;
    private final CallIdGenerator callIdGenerator;
    private final StorageWriter storage;

    private AgentContext() {
        this.callIdGenerator = new CallIdGenerator();
        this.settings = Settings.fromSystemProperties();
        this.startRecordingPolicy = initializePolicy(settings.getStartRecordingPolicyPropertyValue());
        this.storage = settings.buildStorageWriter();
        if (!settings.isAgentDisabled()) {
            this.storage.write(ProcessMetadata.builder()
                    .classPathFiles(new Classpath().toList())
                    .mainClassName(ProcessMetadata.getMainClassNameFromProp())
                    .pid(System.currentTimeMillis())
                    .build()
            );

            Thread shutdown = new Thread(storage::close);
            Runtime.getRuntime().addShutdownHook(shutdown);
        }
    }

    private static StartRecordingPolicy initializePolicy(String value) {
        if (value == null || value.isEmpty()) {
            return new AlwaysStartRecordingPolicy();
        }
        if (value.startsWith("delay:")) {
            return new DelayBasedRecordingPolicy(Duration.ofSeconds(Integer.parseInt(value.replace("delay=", ""))));
        }
        if (value.startsWith("file:")) {
            return new FileBasedStartRecordingPolicy(Paths.get(value.replace("file:", "")));
        }
        throw new IllegalArgumentException("Unsupported recording policy: " + value);
    }

    public static boolean isLoaded() {
        return agentLoaded;
    }

    public static void setLoaded() {
        agentLoaded = true;
    }

    public StartRecordingPolicy getStartRecordingPolicy() {
        return startRecordingPolicy;
    }

    public static AgentContext getInstance() {
        return instance;
    }

    public StorageWriter getStorage() {
        return storage;
    }

    public CallIdGenerator getCallIdGenerator() {
        return callIdGenerator;
    }

    public Settings getSettings() {
        return settings;
    }
}
