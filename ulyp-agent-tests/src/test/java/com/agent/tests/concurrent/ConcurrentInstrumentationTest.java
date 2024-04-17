package com.agent.tests.concurrent;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrentInstrumentationTest extends AbstractInstrumentationTest {

    @Test
    void shouldInstrumentConcurrently() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestRunner.class)
                        .withMethodToRecord(MethodMatcher.parse("**.zxczxc.*"))
                        .withLogLevel("OFF")
        );

        assertEquals(0, recordingResult.recordings().size());
    }

    public static class TestRunner {

        public static void main(String[] args) throws Exception {
            int classesPerWorker = 250;
            int workers = 4;
            List<Future<?>> futureList = new ArrayList<>();
            ExecutorService executorService = Executors.newFixedThreadPool(workers);
            try {
                CyclicBarrier barrier = new CyclicBarrier(workers);

                for (int workerId = 0; workerId < workers; workerId++) {
                    futureList.add(executorService.submit(() -> {
                        try {
                            barrier.await(10, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        URL[] urls;
                        try {
                            File testClassesDir = Paths.get(".", "build", "classes", "java", "test").toFile();
                            if (!testClassesDir.exists()) {
                                throw new RuntimeException("Directory " + testClassesDir.getAbsoluteFile() + " doesn't exist");
                            }
                            URL url = testClassesDir.toURL();
                            urls = new URL[]{url};
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                        ClassLoader classLoader = new URLClassLoader(urls, null);

                        long startTime = System.currentTimeMillis();
                        for (int i = 0; i < classesPerWorker; i++) {
                            if (i > 0 && i % 100 == 0) {
                                classLoader = new URLClassLoader(urls, null);
                            }
                            String classToLoad = "com.agent.tests.concurrent.classes.X" + (i % 100);
                            try {
                                classLoader.loadClass(classToLoad);
                            } catch (Exception e) {
                                throw new RuntimeException("Class " + classToLoad + " not found, test failed", e);
                            }
                        }
                        long elapsed = System.currentTimeMillis() - startTime;
                        System.out.println("Elapsed: " + elapsed + " ms");
                    }));
                }

                for (Future<?> future : futureList) {
                    future.get(10, TimeUnit.SECONDS);
                }
            } finally {
                executorService.shutdownNow();
                executorService.awaitTermination(1, TimeUnit.MINUTES);
            }
        }
    }
}
