package com.ulyp.agent.util;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import com.ulyp.agent.AgentContext;
import com.ulyp.agent.RecorderInstance;
import com.ulyp.agent.queue.RecordingEventQueue;
import com.ulyp.storage.writer.RecordingDataWriter;

// only used for benchmarks, sometimes it's necessary to wait until all data is flushed
public class AgentHelper {

    public static void syncWriting() {
        AgentContext agentContext = AgentContext.getCtx();
        if (agentContext == null) {
            return;
        }

        try {
            RecordingEventQueue recordingEventQueue = RecorderInstance.instance.getRecordingEventQueue();
            recordingEventQueue.sync(Duration.ofSeconds(60));
            RecordingDataWriter storageWriter = AgentContext.getCtx().getStorageWriter();
            storageWriter.sync(Duration.ofSeconds(60));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public static long estimateBytesWritten() {
        return AgentContext.getCtx().getStorageWriter().estimateBytesWritten();
    }
}
