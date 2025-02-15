package com.agent.tests.recorders.java;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.basic.NullObjectRecord;
import com.ulyp.core.recorders.basic.StringObjectRecord;
import com.ulyp.core.recorders.numeric.IntegralRecord;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;

class ObjectInstrumentationTest extends AbstractInstrumentationTest {

    @Test
    void shouldPrintObjects() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(ObjectTestCases.class)
                        .withMethodToRecord("acceptsTwoObjects")
        );

        assertThat(root.getArgs(), hasSize(2));
        assertThat(root.getArgs().get(0), instanceOf(IdentityObjectRecord.class));
        assertThat(root.getArgs().get(1), instanceOf(IdentityObjectRecord.class));
    }

    @Test
    void shouldChooseValidRecorderForJavaLangObjectAtRuntime() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(ObjectTestCases.class)
                        .withMethodToRecord("acceptsTwoObjects2")
        );

        assertThat(root.getArgs(), hasSize(2));
        assertThat(root.getArgs().get(0), instanceOf(StringObjectRecord.class));
        assertThat(root.getArgs().get(1), instanceOf(IntegralRecord.class));
    }

    @Test
    void shouldPrintNullArguments() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(ObjectTestCases.class)
                        .withMethodToRecord("acceptsTwoNulls")
        );

        assertThat(root.getArgs(), hasSize(2));
        assertThat(root.getArgs().get(0), instanceOf(NullObjectRecord.class));
        assertThat(root.getArgs().get(1), instanceOf(NullObjectRecord.class));
    }

    public static class ObjectTestCases {


        public static void main(String[] args) {
            new ObjectTestCases().acceptsTwoObjects(new Object(), new Object());
            new ObjectTestCases().acceptsTwoObjects2("asdasd", 34);
            new ObjectTestCases().acceptsTwoObjects3(new ObjectTestCases.X(), new ObjectTestCases.Y());
            new ObjectTestCases().acceptsTwoNulls(null, null);
        }

        public void acceptsTwoObjects(Object o1, Object o2) {
            System.out.println(o1);
            System.out.println(o2);
        }

        public void acceptsTwoObjects2(Object o1, Object o2) {
            System.err.println(o1);
            System.err.println(o2);
        }

        public void acceptsTwoObjects3(Object o1, Object o2) {
            System.out.println(o1 + ", " + o2);
        }

        public void acceptsTwoNulls(Object o1, Object o2) {
            System.err.println(o1 + ", " + o2);
        }

        private static class X {
        }

        private static class Y {
            public String toString() {
                return "Y{}";
            }
        }
    }
}
