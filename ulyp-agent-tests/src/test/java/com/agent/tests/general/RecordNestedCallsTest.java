package com.agent.tests.general;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.recorders.numeric.NumberRecord;
import com.ulyp.core.recorders.StringObjectRecord;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.tree.CallRecord;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class RecordNestedCallsTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordAllMethods() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.TestCase.startRecordingFoo"))
        );

        CallRecord rootRecord = recordingResult.getSingleRoot();
        assertThat(rootRecord.getArgs(), Matchers.empty());

        CallRecord nestedCallRecord = rootRecord.getChildren().get(0);
        assertThat(nestedCallRecord.getArgs(), hasItem(instanceOf(StringObjectRecord.class)));

        CallRecord nestedCallRecord2 = nestedCallRecord.getChildren().get(0);
        assertThat(nestedCallRecord2.getArgs(), Matchers.hasSize(2));
        assertThat(nestedCallRecord2.getArgs(), allOf(
                hasItem(instanceOf(StringObjectRecord.class)),
                hasItem(instanceOf(NumberRecord.class)))
        );

        CallRecord nestedCallRecord3 = nestedCallRecord2.getChildren().get(0);
        assertThat(nestedCallRecord3.getArgs(), Matchers.hasSize(3));
        assertThat(nestedCallRecord3.getArgs(), allOf(
                hasItem(instanceOf(StringObjectRecord.class)),
                hasItem(instanceOf(NumberRecord.class)),
                hasItem(instanceOf(NumberRecord.class)))
        );
    }

    public static class TestCase {

        public static void foo3(String text, Integer b, Long c) {
            System.out.println(text + b + c);
        }

        public static void foo2(String text, Integer b) {
            foo3(text, b, 5454L);
        }

        public static void foo(String text) {
            System.out.println(text);
            foo2(text, 56);
        }

        public static void startRecordingFoo() {
            foo("ABC");
        }

        public static void main(String[] args) {
            startRecordingFoo();
        }
    }
}
