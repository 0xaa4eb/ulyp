package com.test.recorders;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.SafeCaller;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.CallRecord;
import com.ulyp.core.recorders.StringObjectRecord;
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
            SafeCaller.call(() -> returnLongString());
        }
    }

    @Test
    public void shouldCutLongStringWhileRecording() {
        CallRecord root = runForkWithUi(
                new ForkProcessBuilder().setMainClassName(TestCases.class).setMethodToRecord("returnLongString")
        );

        StringObjectRecord returnValue = (StringObjectRecord) root.getReturnValue();
        assertThat(returnValue.value().length(), lessThan(1000));
    }
}