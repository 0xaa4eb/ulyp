package com.ulyp.agent;

import org.junit.Test;

import static com.ulyp.agent.CallIdGenerator.MAX_CALLS_PER_RECORD_LOG;
import static org.junit.Assert.assertEquals;

public class CallIdGeneratorTest {

    private final CallIdGenerator generator = new CallIdGenerator();

    @Test
    public void testNextValue() {

        assertEquals(1L, generator.getNextStartValue());

        assertEquals(MAX_CALLS_PER_RECORD_LOG + 1L, generator.getNextStartValue());

        assertEquals(2 * MAX_CALLS_PER_RECORD_LOG + 1L, generator.getNextStartValue());
    }
}