package com.ulyp.agent;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import com.ulyp.agent.queue.CallRecordQueue;
import com.ulyp.storage.writer.RecordingDataWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgentShutdownHook implements Runnable {

    @Override
    public void run() {
        AgentContext ctx = AgentContext.getCtx();
        if (!ctx.getSettings().isAgentEnabled()) {
            return;
        }

        CallRecordQueue callRecordQueue = ctx.getCallRecordQueue();
        try {
            callRecordQueue.sync(Duration.ofSeconds(30));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            // ignore
        }

        RecordingDataWriter storageWriter = ctx.getStorageWriter();
        try {
            storageWriter.sync(Duration.ofSeconds(30));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            // ignore
        }
        storageWriter.close();
    }
}
