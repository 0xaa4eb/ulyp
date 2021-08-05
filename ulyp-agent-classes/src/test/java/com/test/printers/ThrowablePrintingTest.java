package com.test.printers;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.SafeCaller;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.CallRecord;
import com.ulyp.core.printers.NullObjectRepresentation;
import com.ulyp.core.printers.StringObjectRepresentation;
import com.ulyp.core.printers.ThrowableRepresentation;
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

        ThrowableRepresentation returnValue = (ThrowableRepresentation) root.getReturnValue();
        StringObjectRepresentation representation = (StringObjectRepresentation) returnValue.getMessage();
        Assert.assertEquals("some exception message", representation.value());
    }

    @Test
    public void shouldHandleNullMessageInThrowable() {
        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(ThrowableTestCases.class)
                        .setMethodToRecord("throwsNullPointerException")
        );

        ThrowableRepresentation returnValue = (ThrowableRepresentation) root.getReturnValue();
        NullObjectRepresentation representation = (NullObjectRepresentation) returnValue.getMessage();
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
