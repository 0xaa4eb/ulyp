package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.StringObjectRecord;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

class StringRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordShortString() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(StringTestCases.class)
                        .withMethodToRecord("returnShortString")
        );

        StringObjectRecord returnValue = (StringObjectRecord) root.getReturnValue();
        assertThat(returnValue.value(), is("ABCDEF"));
    }

    @Test
    void shouldCutLongStringWhileRecording() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(StringTestCases.class)
                        .withMethodToRecord("returnLongString")
        );

        StringObjectRecord returnValue = (StringObjectRecord) root.getReturnValue();
        assertThat(returnValue.value().length(), lessThan(1000));
    }

    public static class StringTestCases {

        public static String returnShortString() {
            return "ABCDEF";
        }

        public static String returnLongString() {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 20 * 1000; i++) {
                builder.append("a");
            }
            return builder.toString();
        }

        public static void main(String[] args) {
            System.out.println(returnLongString());
            System.out.println(returnShortString());
        }
    }
}
