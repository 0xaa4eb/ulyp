package com.test.cases;

import com.test.cases.util.ForkProcessBuilder;
import com.test.cases.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrentRecordingTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordInConcurrentMode() {
        RecordingResult recordingResult = runForkProcess(
                new ForkProcessBuilder()
                        .setMainClassName(MultithreadedExample.class)
                        .setMethodToRecord(MethodMatcher.parse("*.*"))
                        .setLogLevel("OFF")
        );


        recordingResult.assertRecordingSessionCount(10001);
    }

    public static class MultithreadedExample {

        public static void main(String[] args) throws InterruptedException {
            ExecutorService executorService = Executors.newFixedThreadPool(5);

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
