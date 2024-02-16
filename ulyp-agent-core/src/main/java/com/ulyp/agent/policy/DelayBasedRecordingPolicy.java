package com.ulyp.agent.policy;

import com.ulyp.core.util.NamedThreadFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DelayBasedRecordingPolicy implements StartRecordingPolicy {

    private final LocalDateTime startRecordingTimestamp;
    private volatile boolean canStartRecording = false;

    public DelayBasedRecordingPolicy(Duration delay) {
        this.startRecordingTimestamp = LocalDateTime.now().plusSeconds(delay.getSeconds());

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(
                1,
                NamedThreadFactory.builder()
                        .name("Ulyp-DelayBasedRecordingPolicy")
                        .daemon(true)
                        .build()
        );

        scheduledExecutorService.schedule(
                () -> {
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