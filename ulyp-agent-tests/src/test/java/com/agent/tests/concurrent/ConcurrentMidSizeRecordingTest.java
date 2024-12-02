package com.agent.tests.concurrent;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.tree.CallRecord;
import com.ulyp.storage.tree.Recording;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConcurrentMidSizeRecordingTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordInConcurrentMode() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMain(MultithreadedExample.class)
                        .withMethodToRecord(MethodMatcher.parse("**.RecordedRunner.run"))
                        .withLogLevel("OFF")
        );

        recordingResult.assertRecordingSessionCount(1000);

        List<Recording> recordings = recordingResult.recordings();
        for (Recording recording : recordings) {
            CallRecord root = recording.getRoot();
            List<CallRecord> children = root.getChildren();
            assertTrue(children.size() >= 10);
            assertTrue(children.size() < 1034);

            RecordingMetadata metadata = recording.getMetadata();
            assertTrue(metadata.getRecordingStartedMillis() > 0);
            assertTrue(metadata.getRecordingFinishedMillis() > 0);
            long lifetimeMs = metadata.getRecordingFinishedMillis() - metadata.getRecordingStartedMillis();
            assertTrue(lifetimeMs >= 1);
        }
    }

    public static class MultithreadedExample {

        public static void main(String[] args) throws InterruptedException {
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            for (int i = 0; i < 1000; i++) {
                executorService.submit(() -> new RecordedRunner().run());
            }

            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        }
    }

    public static class RecordedRunner {

        public static volatile int store;
        private final int callsCount = 10 + ThreadLocalRandom.current().nextInt(1024);

        public void run() {
            int res = 0;
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (int i = 0; i < callsCount; i++) {
                res ^= calc(i);
            }
            store = res;
        }

        private int calc(int x) {
            return x * 5;
        }
    }
}
