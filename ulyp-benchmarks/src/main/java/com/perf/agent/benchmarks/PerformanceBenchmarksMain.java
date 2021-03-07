package com.perf.agent.benchmarks;

import com.perf.agent.benchmarks.impl.H2MemDatabaseBenchmark;
import com.perf.agent.benchmarks.impl.SpringHibernateSmallBenchmark;
import com.perf.agent.benchmarks.proc.BenchmarkProcessRunner;
import com.ulyp.core.CallEnterRecordList;
import com.ulyp.transport.TCallRecordLogUploadRequest;
import org.HdrHistogram.Histogram;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PerformanceBenchmarksMain {

    private static final int ITERATIONS_PER_PROFILE = 5;

    public static void main(String[] args) throws Exception {

        List<PerformanceRunResult> runResults = new ArrayList<>();

        runResults.addAll(runBench(H2MemDatabaseBenchmark.class));
        runResults.addAll(runBench(SpringHibernateSmallBenchmark.class));

        for (PerformanceRunResult runResult : runResults) {
            runResult.print();
        }
    }

    private static List<PerformanceRunResult> runBench(Class<? extends Benchmark> benchmarkClazz) throws Exception {
        List<PerformanceRunResult> runResults = new ArrayList<>();

        Benchmark benchmark = benchmarkClazz.newInstance();

        for (BenchmarkProfile profile : benchmark.getProfiles()) {
            Histogram procTimeHistogram = emptyHistogram();
            Histogram recordTimeHistogram = emptyHistogram();
            Histogram recordsCountHistogram = new Histogram(1, 10_000_000, 2);

            for (int i = 0; i < ITERATIONS_PER_PROFILE; i++) {
                int recordsCount = run(benchmarkClazz, profile, procTimeHistogram, recordTimeHistogram);
                recordsCountHistogram.recordValue(recordsCount);
            }

            runResults.add(new PerformanceRunResult(benchmarkClazz, profile, procTimeHistogram, recordTimeHistogram, recordsCountHistogram));
        }

        return runResults;
    }

    private static int run(Class<?> benchmarkClazz, BenchmarkProfile profile, Histogram procTimeHistogram, Histogram recordsTimeHistogram) {

        try (MillisMeasured measured = new MillisMeasured(procTimeHistogram)) {

            BenchmarkProcessRunner.runClassInSeparateJavaProcess(benchmarkClazz, profile);

            TCallRecordLogUploadRequest request = profile.getOutputFile().read().get(0);
            recordsTimeHistogram.recordValue(request.getRecordingInfo().getLifetimeMillis());

            return new CallEnterRecordList(request.getRecordLog().getEnterRecords()).size();
        }
    }

    private static Histogram emptyHistogram() {
        return new Histogram(1, TimeUnit.MINUTES.toMillis(5), 2);
    }
}
