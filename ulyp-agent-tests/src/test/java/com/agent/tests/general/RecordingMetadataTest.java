package com.agent.tests.general;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.storage.Recording;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

public class RecordingMetadataTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordAllMethods() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("foo")
        );


        Collection<Recording> recordings = recordingResult.aggregateByRecordings();


        Assert.assertThat(recordings, hasSize(1));
        Recording recording = recordings.iterator().next();
        System.out.println(recording.getMetadata().getStackTraceElements());

        Assert.assertThat(
            recording.getMetadata().getStackTraceElements(),
            allOf(
                hasItem(containsString("com.agent.tests.general.RecordingMetadataTest$TestCase.foo")),
                hasItem(containsString("com.agent.tests.general.RecordingMetadataTest$TestCase.bar")),
                hasItem(containsString("com.agent.tests.general.RecordingMetadataTest$TestCase.zaq")),
                hasItem(containsString("com.agent.tests.general.RecordingMetadataTest$TestCase.main"))
            )
        );
        Assert.assertThat(recording.getLifetime().toMillis(), greaterThan(200L));
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
