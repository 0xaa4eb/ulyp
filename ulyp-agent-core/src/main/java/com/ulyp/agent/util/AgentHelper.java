package com.ulyp.agent.util;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import com.ulyp.agent.AgentContext;
import com.ulyp.storage.writer.RecordingDataWriter;

// only used for benchmarks, sometimes it's necessary to wait until all data is flushed
public class AgentHelper {

    public static void syncWriting() {
        AgentContext agentContext = AgentContext.getInstance();
        if (agentContext == null) {
            return;
        }

        try {
            RecordingDataWriter storageWriter = agentContext.getStorageWriter();
            storageWriter.sync(Duration.ofSeconds(60));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public static long estimateBytesWritten() {
        return AgentContext.getInstance().getStorageWriter().estimateBytesWritten();
    }
}
