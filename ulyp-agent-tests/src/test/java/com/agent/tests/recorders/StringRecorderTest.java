package com.agent.tests.recorders;

import com.agent.tests.cases.AbstractInstrumentationTest;
import com.agent.tests.cases.util.ForkProcessBuilder;
import com.ulyp.core.recorders.StringObjectRecord;
import com.ulyp.storage.CallRecord;
import org.junit.Test;

import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

public class StringRecorderTest extends AbstractInstrumentationTest {

    public static class TestCases {

        public static String returnLongString() {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 20 * 1000; i++) {
                builder.append("a");
            }
            return builder.toString();
        }

        public static void main(String[] args) {
            System.out.println(returnLongString());
        }
    }

    @Test
    public void shouldCutLongStringWhileRecording() {
        CallRecord root = run(
                new ForkProcessBuilder().setMainClassName(TestCases.class).setMethodToRecord("returnLongString")
        );

        StringObjectRecord returnValue = (StringObjectRecord) root.getReturnValue();
        assertThat(returnValue.value().length(), lessThan(1000));
    }
}
