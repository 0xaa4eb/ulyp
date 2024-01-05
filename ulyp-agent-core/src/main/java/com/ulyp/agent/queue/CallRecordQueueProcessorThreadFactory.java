package com.ulyp.agent.queue;

import java.util.concurrent.ThreadFactory;

import org.jetbrains.annotations.NotNull;

import com.ulyp.agent.RecorderInstance;

public class CallRecordQueueProcessorThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(@NotNull Runnable r) {
        Thread t = new ProcessorThread(r);
        t.setDaemon(true);
        t.setName("ulyp-recording-queue-processor");
        return t;
    }

    public static class ProcessorThread extends Thread {
        public ProcessorThread(Runnable target) {
            super(target);
        }

        @Override
        public void run() {
            RecorderInstance.instance.disableRecording();

            super.run();
        }
    }
}
