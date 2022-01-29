package com.test.recorders;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.recorders.NullObjectRecord;
import com.ulyp.core.recorders.StringObjectRecord;
import com.ulyp.core.recorders.ThrowableRecord;
import com.ulyp.storage.CallRecord;
import org.junit.Assert;
import org.junit.Test;

public class ThrowableRecorderTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordThrowableWithMessage() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(ThrowableTestCases.class)
                        .setMethodToRecord("throwsRuntimeException")
        );

        ThrowableRecord returnValue = (ThrowableRecord) root.getReturnValue();
        StringObjectRecord representation = (StringObjectRecord) returnValue.getMessage();
        Assert.assertEquals("some exception message", representation.value());
    }

    @Test
    public void shouldHandleNullMessageInThrowable() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(ThrowableTestCases.class)
                        .setMethodToRecord("throwsNullPointerException")
        );

        ThrowableRecord returnValue = (ThrowableRecord) root.getReturnValue();
        NullObjectRecord representation = (NullObjectRecord) returnValue.getMessage();
        Assert.assertNotNull(representation);
    }

    public static class ThrowableTestCases {

        public static void main(String[] args) {
            try {
                new ThrowableTestCases().throwsRuntimeException();
            } catch (Exception e) {

            }
            try {
                new ThrowableTestCases().throwsNullPointerException();
            } catch (Exception e) {

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
