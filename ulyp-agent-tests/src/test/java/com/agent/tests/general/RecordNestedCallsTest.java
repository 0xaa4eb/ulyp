package com.agent.tests.general;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.recorders.StringObjectRecord;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.tree.CallRecord;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

class RecordNestedCallsTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordAllMethods() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.TestCase.startRecordingFoo"))
        );

        CallRecord rootRecord = recordingResult.getSingleRoot();
        assertThat(rootRecord.getArgs(), Matchers.empty());
        CallRecord nestedCallRecord = rootRecord.getChildren().get(0);
        assertThat(nestedCallRecord.getArgs(), hasItem(instanceOf(StringObjectRecord.class)));
    }

    public static class TestCase {

        public static void foo(String text) {
            System.out.println(text);
        }

        public static void startRecordingFoo() {
            foo("ABC");
        }

        public static void main(String[] args) {
            startRecordingFoo();
        }
    }
}
