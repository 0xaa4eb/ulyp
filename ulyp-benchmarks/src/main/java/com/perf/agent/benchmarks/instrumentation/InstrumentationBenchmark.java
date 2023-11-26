package com.perf.agent.benchmarks.instrumentation;

import com.perf.agent.benchmarks.Benchmark;
import com.perf.agent.benchmarks.BenchmarkScenario;
import com.perf.agent.benchmarks.BenchmarkScenarioBuilder;
import com.ulyp.core.util.MethodMatcher;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InstrumentationBenchmark implements Benchmark {

    private static final int WARMUP_ITERATIONS = 20;
    private static final int ITERATIONS = 5;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        InstrumentationBenchmark benchmark = new InstrumentationBenchmark();
        benchmark.run();

        System.out.println("Took: " + (System.currentTimeMillis() - start) + " ms");
    }

    @Override
    public List<BenchmarkScenario> getProfiles() {
        return Arrays.asList(
                new BenchmarkScenarioBuilder()
                        .withMethodToRecord(new MethodMatcher(InstrumentationBenchmark.class, "zxc"))
                        .build()
        );
    }

    public void run() {
        List<Class<?>> prevIterationClassesLoaded = null;
        for (int iter = 0; iter < WARMUP_ITERATIONS; iter++) {
            List<Class<?>> classesLoaded = runIteration("warmup", 10, 100);
            if (prevIterationClassesLoaded != null) {
                if (prevIterationClassesLoaded.get(0) == classesLoaded.get(0)) {
                    throw new RuntimeException("Same classes loaded");
                }
            }
            prevIterationClassesLoaded = classesLoaded;
        }
        for (int iter = 0; iter < ITERATIONS; iter++) {
            runIteration("measure", 10, 100);
        }
    }

    public List<Class<?>> runIteration(String name, int classLoaders, int classesCountPerClassLoader) {
        long start = System.currentTimeMillis();
        URL[] urls;
        try {
            URL url = Paths.get(".", "ulyp-benchmarks", "build", "classes", "java", "main").toFile().toURL();
            urls = new URL[]{url};
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        List<Class<?>> classes = new ArrayList<>(classLoaders * classesCountPerClassLoader);
        for (int loaderIdx = 0; loaderIdx < classLoaders; loaderIdx++) {

            ClassLoader cl = new URLClassLoader(urls, null);
            for (int clIdx = 0; clIdx < 100; clIdx++) {
                try {
                    classes.add(cl.loadClass("com.perf.agent.benchmarks.instrumentation.classes.X" + clIdx));
                } catch (Exception e) {
                    throw new RuntimeException("Class not found, test failed", e);
                }
            }
        }
        System.out.println("Elapsed (" + name + "): " + (System.currentTimeMillis() - start) + " ms");
        return classes;
    }
}