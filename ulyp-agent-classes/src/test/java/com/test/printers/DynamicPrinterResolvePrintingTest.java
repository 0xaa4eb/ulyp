package com.test.printers;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.CallRecord;
import com.ulyp.core.printers.BooleanRepresentation;
import com.ulyp.core.printers.StringObjectRepresentation;
import org.junit.Assert;
import org.junit.Test;

public class DynamicPrinterResolvePrintingTest extends AbstractInstrumentationTest {

    @Test
    public void shouldUseBooleanPrinterIfBooleanIsPassed() {

        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord("passBoolean")
        );

        BooleanRepresentation repr = (BooleanRepresentation) root.getArgs().get(0);

        Assert.assertTrue(repr.value());
    }

    @Test
    public void shouldUseStringPrinterIfBooleanIsPassed() {

        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord("passString")
        );

        StringObjectRepresentation objectRepresentation = (StringObjectRepresentation) root.getArgs().get(0);

        Assert.assertEquals("ABC", objectRepresentation.value());
    }

    static class TestCase {

        public static void passBoolean(Object val) {
            System.out.println(val);
        }

        public static void passString(Object val) {
            System.out.println(val);
        }

        public static void main(String[] args) {
            passBoolean(true);
            passString("ABC");
        }
    }
}
