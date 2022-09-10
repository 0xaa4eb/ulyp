package com.perf.agent.benchmarks.benchmarks;

import com.perf.agent.benchmarks.Benchmark;
import com.perf.agent.benchmarks.BenchmarkProfileBuilder;
import com.perf.agent.benchmarks.BenchmarkScenario;
import com.ulyp.core.util.MethodMatcher;

import java.util.Arrays;
import java.util.List;

public class FibonacciNumbersBenchmark implements Benchmark {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        FibonacciNumbersBenchmark benchmark = new FibonacciNumbersBenchmark();
        benchmark.setUp();
        benchmark.run();
        benchmark.tearDown();

        System.out.println("Took: " + (System.currentTimeMillis() - start));
    }

    @Override
    public List<BenchmarkScenario> getProfiles() {
        return Arrays.asList(
                new BenchmarkProfileBuilder()
                        .withMethodToRecord(new MethodMatcher(FibonacciNumbersBenchmark.class, "main"))
                        .build(),
                new BenchmarkProfileBuilder()
                        .withAgentDisabled()
                        .build()
        );
    }

    public void setUp() {

    }

    public void tearDown() {

    }

    private int compute(int x) {
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

    public void run() {
        System.out.println(compute(30));
    }
}