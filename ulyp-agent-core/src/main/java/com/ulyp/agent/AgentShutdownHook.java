package com.ulyp.agent;

import com.ulyp.agent.queue.RecordingEventQueue;
import com.ulyp.core.util.ByteSize;
import com.ulyp.storage.writer.RecordingDataWriter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Slf4j
public class AgentShutdownHook implements Runnable {

    @Override
    public void run() {
        AgentContext ctx = AgentContext.getCtx();
        if (!ctx.getOptions().isAgentEnabled()) {
            return;
        }

        log.info("Shutting down the agent conext. Will wait for recording data to be flushed to disk...");

        RecordingEventQueue recordingEventQueue = ctx.getRecordingEventQueue();
        try {
            recordingEventQueue.sync(Duration.ofSeconds(30));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            // ignore
        }
        recordingEventQueue.close();

        RecordingDataWriter storageWriter = ctx.getStorageWriter();
        try {
            storageWriter.sync(Duration.ofSeconds(30));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            // ignore
        }
        long totalBytesWritten = storageWriter.estimateBytesWritten();
        log.info("Total bytes written to file: {}", ByteSize.toHumanReadable(totalBytesWritten));
        storageWriter.close();
    }
}
