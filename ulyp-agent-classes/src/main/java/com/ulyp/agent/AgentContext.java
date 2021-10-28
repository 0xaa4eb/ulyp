package com.ulyp.agent;

import com.ulyp.agent.transport.UiTransport;
import com.ulyp.core.process.ProcessInfo;

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
    private final UiTransport transport;
    private final ProcessInfo processInfo;

    private AgentContext() {
        this.settings = Settings.fromSystemProperties();
        this.processInfo = new ProcessInfo();
        this.transport = settings.buildUiTransport();

        Thread shutdown = new Thread(
                () -> {
                    try {
                        transport.shutdownNowAndAwaitForRecordsLogsSending(30, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
        );
        Runtime.getRuntime().addShutdownHook(shutdown);
    }

    public ProcessInfo getProcessInfo() {
        return processInfo;
    }

    public UiTransport getTransport() {
        return transport;
    }

    public Settings getSettings() {
        return settings;
    }
}
