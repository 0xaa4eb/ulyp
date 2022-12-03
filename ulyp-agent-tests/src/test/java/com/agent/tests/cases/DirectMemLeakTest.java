package com.agent.tests.cases;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class DirectMemLeakTest extends AbstractInstrumentationTest {

    @Test
    public void shouldWithstandLongRecording() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(FibonacciTestCase.class)
                        .withMethodToRecord("compute")
        );


        recordingResult.assertRecordingSessionCount(10001);
    }

    public static class FibonacciTestCase {

        private static int compute(int x) {
            if (x < 0) {
                throw new IllegalArgumentException("Should be positive");
            }
            if (x == 1) {
                return 1;
            }
            if (x == 0) {
                return 1;
            }
            return compute(x - 2) + compute(x - 1);
        }

        public static void main(String[] args) throws InterruptedException {
            System.out.println(compute(35));
        }
    }
}