package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.NumberRecord;
import com.ulyp.storage.CallRecord;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BoxedNumbersRecorderTest extends AbstractInstrumentationTest {

    public static class BoxedNumbersTestCases {

        public static int primitiveIntSum(int v1, int v2) {
            return v1 + v2;
        }

        public static double primitiveDoubleSum(double v1, double v2) {
            return v1 + v2;
        }

        public static Integer boxedIntSum(Integer v1, Integer v2) {
            return v1 + v2;
        }

        public static Double boxedDoubleSum(Double v1, Double v2) {
            return v1 + v2;
        }

        public static void te(Long v1) {

        }

        public static void main(String[] args) {
            System.out.println(BoxedNumbersTestCases.boxedIntSum(Integer.valueOf(-234), Integer.valueOf(23)));
            System.out.println(BoxedNumbersTestCases.boxedDoubleSum(Double.valueOf(-5434.23), Double.valueOf(321.2453)));
            System.out.println(BoxedNumbersTestCases.primitiveDoubleSum(Double.valueOf(-5434.23), Double.valueOf(321.2453)));
            System.out.println(BoxedNumbersTestCases.primitiveIntSum(-234, 23));
            BoxedNumbersTestCases.te(1L);
        }
    }

    @Test
    public void testBoxedLong() {
        CallRecord root = run(
                new ForkProcessBuilder().withMainClassName(BoxedNumbersTestCases.class)
                        .withMethodToRecord("te")
        );

        NumberRecord arg = (NumberRecord) root.getArgs().get(0);
        assertThat(arg.getNumberPrintedText(), is("1"));
    }

    @Test
    public void testPrimitiveIntSum() {
        CallRecord root = run(
                new ForkProcessBuilder().withMainClassName(BoxedNumbersTestCases.class)
                        .withMethodToRecord("primitiveIntSum")
        );

        assertThat(((NumberRecord) root.getArgs().get(0)).getNumberPrintedText(), is("-234"));
        assertThat(((NumberRecord) root.getArgs().get(1)).getNumberPrintedText(), is("23"));
        assertThat(((NumberRecord) root.getReturnValue()).getNumberPrintedText(), is("-211"));
    }

    @Test
    public void testBoxedIntSum() {
        CallRecord root = run(
                new ForkProcessBuilder().withMainClassName(BoxedNumbersTestCases.class)
                        .withMethodToRecord("boxedIntSum")
        );

        assertThat(((NumberRecord) root.getReturnValue()).getNumberPrintedText(), is("-211"));
    }

    @Test
    public void testPrimitiveDoubleSum() {
        CallRecord root = run(
                new ForkProcessBuilder().withMainClassName(BoxedNumbersTestCases.class)
                        .withMethodToRecord("primitiveDoubleSum")
        );

        assertThat(((NumberRecord) root.getArgs().get(0)).getNumberPrintedText(), is("-5434.23"));
        assertThat(((NumberRecord) root.getArgs().get(1)).getNumberPrintedText(), is("321.2453"));
        assertThat(((NumberRecord) root.getReturnValue()).getNumberPrintedText(), is("-5112.9847"));
    }

    @Test
    public void testBoxedDoubleSum() {
        CallRecord root = run(
                new ForkProcessBuilder().withMainClassName(BoxedNumbersTestCases.class)
                        .withMethodToRecord("boxedDoubleSum")
        );

        assertThat(((NumberRecord) root.getArgs().get(0)).getNumberPrintedText(), is("-5434.23"));
        assertThat(((NumberRecord) root.getArgs().get(1)).getNumberPrintedText(), is("321.2453"));
        assertThat(((NumberRecord) root.getReturnValue()).getNumberPrintedText(), is("-5112.9847"));
    }
}
