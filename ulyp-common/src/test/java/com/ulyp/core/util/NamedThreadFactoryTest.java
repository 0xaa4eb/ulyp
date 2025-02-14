package com.ulyp.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NamedThreadFactoryTest {

    @Test
    void shouldCreateNonDaemonThreadsWithCorrectNames() {
        // Given
        NamedThreadFactory factory = NamedThreadFactory.builder()
                .name("worker")
                .daemon(false)
                .build();

        // When
        Thread thread1 = factory.newThread(() -> {});
        Thread thread2 = factory.newThread(() -> {});
        Thread thread3 = factory.newThread(() -> {});

        // Then
        assertAll(
            () -> assertEquals("worker-0", thread1.getName(), 
                "First thread should have index 0"),
            () -> assertEquals("worker-1", thread2.getName(), 
                "Second thread should have index 1"),
            () -> assertEquals("worker-2", thread3.getName(), 
                "Third thread should have index 2"),
            () -> assertFalse(thread1.isDaemon(), 
                "Thread should not be daemon when daemon=false"),
            () -> assertFalse(thread2.isDaemon(), 
                "Thread should not be daemon when daemon=false"),
            () -> assertFalse(thread3.isDaemon(), 
                "Thread should not be daemon when daemon=false")
        );
    }

    @Test
    void shouldCreateDaemonThreadsWithCorrectNames() {
        // Given
        NamedThreadFactory factory = NamedThreadFactory.builder()
                .name("daemon-worker")
                .daemon(true)
                .build();

        // When
        Thread thread1 = factory.newThread(() -> {});
        Thread thread2 = factory.newThread(() -> {});

        // Then
        assertAll(
            () -> assertEquals("daemon-worker-0", thread1.getName(), 
                "First daemon thread should have index 0"),
            () -> assertEquals("daemon-worker-1", thread2.getName(), 
                "Second daemon thread should have index 1"),
            () -> assertTrue(thread1.isDaemon(), 
                "Thread should be daemon when daemon=true"),
            () -> assertTrue(thread2.isDaemon(), 
                "Thread should be daemon when daemon=true")
        );
    }

    @Test
    void shouldIncrementThreadIndexSequentially() {
        // Given
        NamedThreadFactory factory = NamedThreadFactory.builder()
                .name("counter")
                .daemon(false)
                .build();

        // When/Then
        for (int i = 0; i < 1000; i++) {
            Thread thread = factory.newThread(() -> {});
            assertEquals("counter-" + i, thread.getName(),
                "Thread index should increment sequentially");
        }
    }
} 