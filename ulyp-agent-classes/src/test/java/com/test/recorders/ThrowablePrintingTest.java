package com.test.recorders;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.SafeCaller;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.CallRecord;
import com.ulyp.core.recorders.NullObjectRecord;
import com.ulyp.core.recorders.StringObjectRecord;
import com.ulyp.core.recorders.ThrowableRecord;
import org.junit.Assert;
import org.junit.Test;

public class ThrowablePrintingTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordThrowableWithMessage() {
        CallRecord root = runForkWithUi(
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
        CallRecord root = runForkWithUi(
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
            SafeCaller.call(() -> new ThrowableTestCases().throwsRuntimeException());
            SafeCaller.call(() -> new ThrowableTestCases().throwsNullPointerException());
        }

        public int throwsRuntimeException() {
            throw new RuntimeException("some exception message");
        }

        public int throwsNullPointerException() {
            throw new NullPointerException();
        }
    }
}
