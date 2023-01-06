package com.agent.tests.general;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.storage.Recording;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.Matchers.greaterThan;
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
        Assert.assertThat(recordings.iterator().next().getLifetime().toMillis(), greaterThan(200L));
    }

    public static class TestCase {

        public static int foo() {
            try {
                Thread.sleep(220);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 42;
        }

        public static void main(String[] args) throws InterruptedException {
            System.out.println(TestCase.foo());
        }
    }
}
