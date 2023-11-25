package com.agent.tests.concurrent;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ConcurrentInstrumentationTest extends AbstractInstrumentationTest {

    @Test
    public void shouldInstrumentConcurrently() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestRunner.class)
                        .withMethodToRecord(MethodMatcher.parse("**.X5.*"))
                        .withLogLevel("OFF")
        );

        Assert.assertEquals(0, recordingResult.recordings().size());
    }

    public static class TestRunner {

        public static void main(String[] args) throws Exception {
            int classesPerWorker = 250;
            int workers = 4;
            List<Future<?>> futureList = new ArrayList<>();
            ExecutorService executorService = Executors.newFixedThreadPool(workers);
            CyclicBarrier barrier = new CyclicBarrier(workers);

            for (int workerId = 0; workerId < workers; workerId++) {
                int workerIdFinal = workerId;
                futureList.add(executorService.submit(() -> {
                    try {
                        barrier.await(1, TimeUnit.MINUTES);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    long startTime = System.currentTimeMillis();
                    for (int num = workerIdFinal * classesPerWorker; num < (workerIdFinal + 1) * classesPerWorker; num++) {
                        try {
                            Class<?> aClass = Class.forName("com.agent.tests.concurrent.classes.X" + num);
                            aClass.newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException("Class not found, test failed", e);
                        }
                    }
                    long elapsed = System.currentTimeMillis() - startTime;
                    System.out.println("Elapsed: " + elapsed + " ms");
                }));
            }

            for (Future<?> future : futureList) {
                future.get(1, TimeUnit.MINUTES);
            }

            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        }
    }
}
