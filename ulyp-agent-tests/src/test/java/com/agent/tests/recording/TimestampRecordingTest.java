package com.agent.tests.recording;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TimestampRecordingTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordTimestamp() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnChar")
                        .withRecordTimestamps(true)
        );

        assertTrue(root.getNanosDuration() > 0);
    }

    @Test
    void shouldRecordTimestampLongerTime() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("sleep")
                        .withRecordTimestamps(true)
        );

        assertTrue(root.getNanosDuration() > Duration.ofMillis(100).getNano());
    }


    public static class TestCase {

        public static void main(String[] args) {
            System.out.println(returnChar());
            System.out.println(sleep());
        }

        public static char returnChar() {
            return 'A';
        }

        public static char sleep() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return 'A';
        }
    }
}
