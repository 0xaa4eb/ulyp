package com.perf.agent.benchmarks;

import com.perf.agent.benchmarks.benchmarks.H2MemDatabaseBenchmark;
import com.perf.agent.benchmarks.benchmarks.SpringHibernateSmallBenchmark;
import com.perf.agent.benchmarks.proc.BenchmarkProcessRunner;
import com.perf.agent.benchmarks.proc.OutputFile;
import com.ulyp.storage.StorageReader;
import org.HdrHistogram.Histogram;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class BenchmarksMain {

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

        for (BenchmarkScenario profile : benchmark.getProfiles()) {
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

    private static int run(Class<?> benchmarkClazz, BenchmarkScenario profile, Histogram procTimeHistogram, Histogram recordsTimeHistogram) {

        try (TimeMeasurer measured = new TimeMeasurer(procTimeHistogram)) {

            BenchmarkProcessRunner.runClassInSeparateJavaProcess(benchmarkClazz, profile);

            StorageReader storageReader = Optional.ofNullable(profile.getOutputFile()).map(OutputFile::toReader).orElse(StorageReader.empty());
            if (!storageReader.availableRecordings().isEmpty()) {
                recordsTimeHistogram.recordValue(storageReader.availableRecordings().get(0).getLifetime().toMillis());
                return storageReader.availableRecordings().get(0).callCount();
            } else {
                recordsTimeHistogram.recordValue(0L);
                return 0;
            }
        }
    }

    private static Histogram emptyHistogram() {
        return new Histogram(1, TimeUnit.MINUTES.toMillis(5), 2);
    }
}
