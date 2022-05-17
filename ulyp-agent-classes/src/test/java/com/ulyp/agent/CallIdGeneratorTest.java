package com.ulyp.agent;

import org.junit.Test;

import static com.ulyp.agent.CallIdGenerator.MAX_CALLS_PER_RECORD_LOG;
import static org.junit.Assert.*;

public class CallIdGeneratorTest {

    private final CallIdGenerator generator = new CallIdGenerator();

    @Test
    public void testNextValue() {

        assertEquals(0L, generator.getNextStartValue());

        assertEquals(MAX_CALLS_PER_RECORD_LOG, generator.getNextStartValue());

        assertEquals(2 * MAX_CALLS_PER_RECORD_LOG, generator.getNextStartValue());
    }
}