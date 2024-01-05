package com.ulyp.agent.util;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import com.ulyp.agent.AgentContext;
import com.ulyp.agent.RecorderInstance;
import com.ulyp.agent.queue.CallRecordQueue;
import com.ulyp.storage.writer.RecordingDataWriter;

// only used for benchmarks, sometimes it's necessary to wait until all data is flushed
public class AgentHelper {

    public static void syncWriting() throws InterruptedException, TimeoutException {
        CallRecordQueue callRecordQueue = RecorderInstance.instance.getCallRecordQueue();
        callRecordQueue.sync(Duration.ofSeconds(60));
        RecordingDataWriter storageWriter = AgentContext.getInstance().getStorageWriter();
        storageWriter.sync(Duration.ofSeconds(60));
    }
}
