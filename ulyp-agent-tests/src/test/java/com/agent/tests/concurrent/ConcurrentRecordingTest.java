package com.agent.tests.concurrent;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class ConcurrentRecordingTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordInConcurrentMode() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(MultithreadedExample.class)
                        .withMethodToRecord(MethodMatcher.parse("*.*"))
                        .withLogLevel("OFF")
        );

        recordingResult.assertRecordingSessionCount(10001);
    }

    public static class MultithreadedExample {

        public static void main(String[] args) throws InterruptedException {
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            for (int i = 0; i < 10000; i++) {
                executorService.submit(
                        () -> new Clazz().bar()
                );
            }

            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        }
    }

    public static class Clazz {

        public int bar() {
            return 1;
        }
    }
}
