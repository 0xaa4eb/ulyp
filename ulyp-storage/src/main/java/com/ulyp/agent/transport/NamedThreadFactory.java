package com.ulyp.agent.transport;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class NamedThreadFactory implements ThreadFactory {

    private final String name;
    private final boolean daemon;
    private final AtomicLong ctr = new AtomicLong(-1);

    public NamedThreadFactory(String name, boolean daemon) {
        this.name = name;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, name + "-" + ctr.incrementAndGet());
        t.setDaemon(daemon);
        return t;
    }
}
