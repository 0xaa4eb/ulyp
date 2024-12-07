package com.ulyp.agent.queue;

import com.ulyp.agent.AgentContext;
import com.ulyp.agent.RecorderInstance;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;

public class QueueEventHandlerThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(@NotNull Runnable r) {
        Thread t = new HandlerThread(r);
        t.setDaemon(true);
        t.setName("ulyp-recording-queue-processor");
        return t;
    }

    private static class HandlerThread extends Thread {
        public HandlerThread(Runnable target) {
            super(target);
        }

        @Override
        public void run() {
            if (AgentContext.isLoaded()) {
                RecorderInstance.instance.disableRecording();
            }

            super.run();
        }
    }
}
