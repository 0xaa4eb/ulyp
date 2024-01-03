package com.ulyp.agent.queue;

import java.util.concurrent.ThreadFactory;

import org.jetbrains.annotations.NotNull;

public class CallRecordQueueProcessorThread implements ThreadFactory {

    @Override
    public Thread newThread(@NotNull Runnable r) {
        return null;
    }
}
