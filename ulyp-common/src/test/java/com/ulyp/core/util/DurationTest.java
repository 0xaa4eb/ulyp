package com.ulyp.core.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DurationTest {

    @Test
    void shouldFormatNanoseconds() {
        assertEquals("1 ns", Duration.printNanos(1), "Should format 1 nanosecond correctly");
        assertEquals("999 ns", Duration.printNanos(999), "Should format 999 nanoseconds correctly");
    }

    @Test
    void shouldFormatMicroseconds() {
        assertEquals("1 us", Duration.printNanos(1_000), "Should format 1 microsecond correctly");
        assertEquals("500 us", Duration.printNanos(500_000), "Should format 500 microseconds correctly");
        assertEquals("1000 us", Duration.printNanos(999_999), "Should format 999 microseconds correctly");
    }

    @Test
    void shouldFormatMilliseconds() {
        assertEquals("1 ms", Duration.printNanos(1_000_000), "Should format 1 millisecond correctly");
        assertEquals("500 ms", Duration.printNanos(500_000_000), "Should format 500 milliseconds correctly");
        assertEquals("1000 ms", Duration.printNanos(999_999_999), "Should format 999 milliseconds correctly");
    }

    @Test
    void shouldFormatSeconds() {
        assertEquals("1 s", Duration.printNanos(1_000_000_000), "Should format 1 second correctly");
        assertEquals("5 s", Duration.printNanos(5_000_000_000L), "Should format 5 seconds correctly");
        assertEquals("60 s", Duration.printNanos(60_000_000_000L), "Should format 60 seconds correctly");
    }

    @Test
    void shouldHandleZero() {
        assertEquals("0 ns", Duration.printNanos(0), "Should format zero duration correctly");
    }

    @Test
    void shouldHandleObjectCreation() {
        Duration duration = new Duration(5_000_000); // 5 milliseconds
        assertEquals("5 ms", duration.toString(), "Duration object should format 5 milliseconds correctly");
    }
} 