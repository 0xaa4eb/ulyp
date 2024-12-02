package com.agent.tests.recorders.java;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.basic.NullObjectRecord;
import com.ulyp.core.recorders.basic.StringObjectRecord;
import com.ulyp.core.recorders.basic.ThrowableRecord;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ThrowableRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordThrowableWithMessage() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(ThrowableTestCases.class)
                        .withMethodToRecord("throwsRuntimeException")
        );

        ThrowableRecord returnValue = (ThrowableRecord) root.getReturnValue();
        StringObjectRecord representation = (StringObjectRecord) returnValue.getMessage();
        assertEquals("some exception message", representation.value());
    }

    @Test
    void shouldHandleNullMessageInThrowable() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(ThrowableTestCases.class)
                        .withMethodToRecord("throwsNullPointerException")
        );

        ThrowableRecord returnValue = (ThrowableRecord) root.getReturnValue();
        NullObjectRecord representation = (NullObjectRecord) returnValue.getMessage();
        assertNotNull(representation);
    }

    public static class ThrowableTestCases {

        public static void main(String[] args) {
            try {
                new ThrowableTestCases().throwsRuntimeException();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                new ThrowableTestCases().throwsNullPointerException();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public int throwsRuntimeException() {
            throw new RuntimeException("some exception message");
        }

        public int throwsNullPointerException() {
            throw new NullPointerException();
        }
    }
}
