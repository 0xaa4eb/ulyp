package com.agent.tests.general;

import java.util.Collection;

import org.junit.jupiter.api.Test;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.storage.tree.Recording;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

class StackTraceRecordingTest extends AbstractInstrumentationTest {

    @Test
    void testStackTracer() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("foo")
        );


        Collection<Recording> recordings = recordingResult.aggregateByRecordings();


        assertThat(recordings, hasSize(1));
        Recording recording = recordings.iterator().next();
        System.out.println(recording.getMetadata().getStackTraceElements());

        assertThat(recording.getMetadata().getStackTraceElements(), allOf(
            hasItem(containsString("com.agent.tests.general.StackTraceRecordingTest$TestCase.foo")),
            hasItem(containsString("com.agent.tests.general.StackTraceRecordingTest$TestCase.bar")),
            hasItem(containsString("com.agent.tests.general.StackTraceRecordingTest$TestCase.zaq")),
            hasItem(containsString("com.agent.tests.general.StackTraceRecordingTest$TestCase.main"))
        ));
        assertThat(recording.getLifetime().toMillis(), greaterThan(200L));
    }

    public static class TestCase {

        public static int zaq() {
            return bar();
        }

        public static int bar() {
            return foo();
        }

        public static int foo() {
            try {
                Thread.sleep(220);
            } catch (InterruptedException e) {
                // NOP
            }
            return 42;
        }

        public static void main(String[] args) throws InterruptedException {
            System.out.println(TestCase.zaq());
        }
    }
}
