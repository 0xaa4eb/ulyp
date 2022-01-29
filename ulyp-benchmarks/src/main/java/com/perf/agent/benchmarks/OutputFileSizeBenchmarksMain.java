package com.perf.agent.benchmarks;

import com.perf.agent.benchmarks.impl.H2MemDatabaseBenchmark;
import com.perf.agent.benchmarks.impl.SpringHibernateSmallBenchmark;
import com.perf.agent.benchmarks.proc.BenchmarkProcessRunner;

import java.util.ArrayList;
import java.util.List;

public class OutputFileSizeBenchmarksMain {

    public static void main(String[] args) throws Exception {

        List<OutputFileSizeResult> runResults = new ArrayList<>();

        runResults.addAll(runBench(H2MemDatabaseBenchmark.class));
        runResults.addAll(runBench(SpringHibernateSmallBenchmark.class));

        for (OutputFileSizeResult runResult : runResults) {
            System.out.println(runResult);
        }
    }

    private static List<OutputFileSizeResult> runBench(Class<? extends Benchmark> benchmarkClazz) throws Exception {
        List<OutputFileSizeResult> runResults = new ArrayList<>();

        Benchmark benchmark = benchmarkClazz.newInstance();

        for (BenchmarkProfile profile : benchmark.getProfiles()) {
            if (!profile.shouldWriteRecording()) {
                // If nothing is sent to UI, then there is nothing to measure
                continue;
            }

            runResults.add(new OutputFileSizeResult(benchmarkClazz, profile, run(benchmarkClazz, profile)));
        }

        return runResults;
    }

    private static long run(Class<?> benchmarkClazz, BenchmarkProfile profile) {
        BenchmarkProcessRunner.runClassInSeparateJavaProcess(benchmarkClazz, profile);

        return profile.getOutputFile().byteSize();
    }
}
