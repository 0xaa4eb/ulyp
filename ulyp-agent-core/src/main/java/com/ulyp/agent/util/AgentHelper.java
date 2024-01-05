package com.ulyp.agent.util;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import com.ulyp.agent.AgentContext;
import com.ulyp.storage.writer.RecordingDataWriter;

// only used for benchmarks, sometimes it's necessary to wait until all data is flushed
public class AgentHelper {

    public static void syncWriting() throws InterruptedException, TimeoutException {
        AgentContext agentContext = AgentContext.getInstance();

        RecordingDataWriter storageWriter = agentContext.getStorageWriter();
        storageWriter.sync(Duration.ofSeconds(60));
    }
}
