package com.ulyp.agent.policy;

import java.util.regex.Pattern;

public class ThreadNameRecordingPolicy implements StartRecordingPolicy {

    private final StartRecordingPolicy delegate;
    private final Pattern threadNamePattern;

    public ThreadNameRecordingPolicy(StartRecordingPolicy delegate, Pattern threadNamePattern) {
        this.delegate = delegate;
        this.threadNamePattern = threadNamePattern;
    }

    @Override
    public boolean canStartRecording() {
        String threadName = Thread.currentThread().getName();
        return threadNamePattern.matcher(threadName).matches() && delegate.canStartRecording();
    }

    @Override
    public String toString() {
        return "Start recording at threads '" + threadNamePattern + "' && " + delegate;
    }
}