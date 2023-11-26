package com.agent.tests.concurrent;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Testss {

    public static void main(String[] args) throws Exception {
        int classesPerWorker = 250;
        int workers = 4;
        List<Future<?>> futureList = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(workers);
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
                    URL url = Paths.get(".", "ulyp-agent-tests", "build", "classes", "java", "test").toFile().toURL();
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
                    try {
                        classLoader.loadClass("com.agent.tests.concurrent.classes.X" + (i % 100));
                    } catch (Exception e) {
                        throw new RuntimeException("Class not found, test failed", e);
                    }
                }
                long elapsed = System.currentTimeMillis() - startTime;
                System.out.println("Elapsed: " + elapsed + " ms");
            }));
        }

        for (Future<?> future : futureList) {
            future.get(10, TimeUnit.SECONDS);
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
    }
}
