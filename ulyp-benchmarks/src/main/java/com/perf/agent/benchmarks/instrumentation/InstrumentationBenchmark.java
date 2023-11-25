package com.perf.agent.benchmarks.instrumentation;

import com.perf.agent.benchmarks.Benchmark;
import com.perf.agent.benchmarks.BenchmarkScenario;
import com.perf.agent.benchmarks.BenchmarkScenarioBuilder;
import com.ulyp.core.util.MethodMatcher;

import java.util.Arrays;
import java.util.List;

public class InstrumentationBenchmark implements Benchmark {

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
        for (int i = 0; i < 1000; i++) {
            try {
                Class<?> aClass = Class.forName("com.perf.agent.benchmarks.instrumentation.classes.X" + i);
                aClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Class not found, test failed", e);
            }
        }
    }
}