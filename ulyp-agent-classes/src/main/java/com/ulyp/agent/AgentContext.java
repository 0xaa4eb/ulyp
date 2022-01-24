package com.ulyp.agent;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.process.Classpath;
import com.ulyp.core.process.ProcessInfo;
import com.ulyp.storage.StorageWriter;

import java.util.concurrent.TimeUnit;

public class AgentContext {

    private static final AgentContext instance = new AgentContext();

    private static volatile boolean agentLoaded = false;

    public static boolean isLoaded() {
        return agentLoaded;
    }

    public static void setLoaded() {
        agentLoaded = true;
    }

    public static AgentContext getInstance() {
        return instance;
    }

    private final Settings settings;
    private final CallIdGenerator callIdGenerator;
    private final StorageWriter storage;

    private AgentContext() {
        this.callIdGenerator = new CallIdGenerator();
        this.settings = Settings.fromSystemProperties();

        this.storage = settings.buildStorageWriter();
        this.storage.write(ProcessMetadata.builder()
                        .classPath(new Classpath().toString())
                        .mainClassName(ProcessMetadata.getMainClassNameFromProp())
                        .build()
        );

        Thread shutdown = new Thread(storage::close);
        Runtime.getRuntime().addShutdownHook(shutdown);
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
