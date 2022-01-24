package com.ulyp.agent.util;

import com.ulyp.storage.util.NamedThreadFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DelayBasedRecordingPolicy implements StartRecordingPolicy {

    private volatile boolean canStartRecording = false;
    private final LocalDateTime startRecordingTimestamp;

    public DelayBasedRecordingPolicy(Duration delay) {
        this.startRecordingTimestamp = LocalDateTime.now().plusSeconds(delay.toMillis());

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(
                1,
                new NamedThreadFactory("Ulyp-DelayBasedRecordingPolicy", true)
        );

        scheduledExecutorService.schedule(
                () -> {
                    System.out.println("ULYP: recording can start now");
                    canStartRecording = true;
                },
                delay.toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public boolean canStartRecording() {
        return canStartRecording;
    }

    @Override
    public String toString() {
        return "Start recording at " + startRecordingTimestamp;
    }
}