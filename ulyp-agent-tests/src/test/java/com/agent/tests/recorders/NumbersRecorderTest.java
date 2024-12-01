package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.NumberRecord;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class NumbersRecorderTest extends AbstractInstrumentationTest {

    @Test
    void testBoxedLong() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder().withMainClassName(BoxedNumbersTestCases.class)
                        .withMethodToRecord("te")
        );

        NumberRecord arg = (NumberRecord) root.getArgs().get(0);
        assertThat(arg.getNumberPrintedText(), is("1"));
    }

    @Test
    void testPrimitiveIntSum() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder().withMainClassName(BoxedNumbersTestCases.class)
                        .withMethodToRecord("primitiveIntSum")
        );

        assertThat(((NumberRecord) root.getArgs().get(0)).getNumberPrintedText(), is("-234"));
        assertThat(((NumberRecord) root.getArgs().get(1)).getNumberPrintedText(), is("23"));
        assertThat(((NumberRecord) root.getReturnValue()).getNumberPrintedText(), is("-211"));
    }

    @Test
    void testBoxedIntSum() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder().withMainClassName(BoxedNumbersTestCases.class)
                        .withMethodToRecord("boxedIntSum")
        );

        assertThat(((NumberRecord) root.getReturnValue()).getNumberPrintedText(), is("-211"));
    }

    @Test
    void testPrimitiveDoubleSum() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder().withMainClassName(BoxedNumbersTestCases.class)
                        .withMethodToRecord("primitiveDoubleSum")
        );

        assertThat(((NumberRecord) root.getArgs().get(0)).getNumberPrintedText(), is("-5434.23"));
        assertThat(((NumberRecord) root.getArgs().get(1)).getNumberPrintedText(), is("321.2453"));
        assertThat(((NumberRecord) root.getReturnValue()).getNumberPrintedText(), is("-5112.9847"));
    }

    @Test
    void testBoxedDoubleSum() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder().withMainClassName(BoxedNumbersTestCases.class)
                        .withMethodToRecord("boxedDoubleSum")
        );

        assertThat(((NumberRecord) root.getArgs().get(0)).getNumberPrintedText(), is("-5434.23"));
        assertThat(((NumberRecord) root.getArgs().get(1)).getNumberPrintedText(), is("321.2453"));
        assertThat(((NumberRecord) root.getReturnValue()).getNumberPrintedText(), is("-5112.9847"));
    }

    @Test
    void testBoxedFloatSum() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder().withMainClassName(BoxedNumbersTestCases.class)
                        .withMethodToRecord("boxedFloatSum")
        );

        assertThat(((NumberRecord) root.getArgs().get(0)).getNumberPrintedText(), is("-5434.23"));
        assertThat(((NumberRecord) root.getArgs().get(1)).getNumberPrintedText(), is("321.2453"));
        assertThat(((NumberRecord) root.getReturnValue()).getNumberPrintedText(), containsString("-5112.98"));
    }

    @Test
    void testPrimitiveFloatSum() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder().withMainClassName(BoxedNumbersTestCases.class)
                        .withMethodToRecord("primitiveFloatSum")
        );

        assertThat(((NumberRecord) root.getArgs().get(0)).getNumberPrintedText(), is("-5434.23"));
        assertThat(((NumberRecord) root.getArgs().get(1)).getNumberPrintedText(), is("321.2453"));
        assertThat(((NumberRecord) root.getReturnValue()).getNumberPrintedText(), containsString("-5112.98"));
    }

    public static class BoxedNumbersTestCases {

        public static int primitiveIntSum(int v1, int v2) {
            return v1 + v2;
        }

        public static double primitiveDoubleSum(double v1, double v2) {
            return v1 + v2;
        }

        public static float primitiveFloatSum(float v1, float v2) {
            return v1 + v2;
        }

        public static Integer boxedIntSum(Integer v1, Integer v2) {
            return v1 + v2;
        }

        public static Float boxedFloatSum(Float v1, Float v2) {
            return v1 + v2;
        }

        public static Double boxedDoubleSum(Double v1, Double v2) {
            return v1 + v2;
        }

        public static void te(Long v1) {

        }

        public static void main(String[] args) {
            System.out.println(BoxedNumbersTestCases.boxedIntSum(-234, 23));
            System.out.println(BoxedNumbersTestCases.boxedDoubleSum(-5434.23, 321.2453));
            System.out.println(BoxedNumbersTestCases.boxedFloatSum(-5434.23f, 321.2453f));
            System.out.println(BoxedNumbersTestCases.primitiveFloatSum(-5434.23f, 321.2453f));
            System.out.println(BoxedNumbersTestCases.primitiveDoubleSum(-5434.23, 321.2453));
            System.out.println(BoxedNumbersTestCases.primitiveIntSum(-234, 23));
            BoxedNumbersTestCases.te(1L);
        }
    }
}
