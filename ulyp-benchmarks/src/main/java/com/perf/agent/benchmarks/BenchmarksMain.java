package com.perf.agent.benchmarks;

import com.perf.agent.benchmarks.benchmarks.*;
import com.perf.agent.benchmarks.proc.BenchmarkProcessRunner;
import com.perf.agent.benchmarks.proc.OutputFile;
import com.ulyp.storage.Recording;
import com.ulyp.storage.StorageReader;
import org.HdrHistogram.Histogram;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class BenchmarksMain {

    private static final int ITERATIONS_PER_PROFILE = 2;

    public static void main(String[] args) throws Exception {

        List<BenchmarkRunResult> runResults = new ArrayList<>();

        runResults.addAll(runBench(H2MemDatabaseBenchmark.class));
        runResults.addAll(runBench(ActivemqBenchmark.class));
        runResults.addAll(runBench(FibonacciNumbersBenchmark.class));
        runResults.addAll(runBench(SpringHibernateSmallBenchmark.class));
        runResults.addAll(runBench(SpringHibernateMediumBenchmark.class));

        for (BenchmarkRunResult runResult : runResults) {
            runResult.print();
        }
    }

    private static List<BenchmarkRunResult> runBench(Class<? extends Benchmark> benchmarkClazz) throws Exception {
        List<BenchmarkRunResult> runResults = new ArrayList<>();

        Benchmark benchmark = benchmarkClazz.newInstance();

        for (BenchmarkScenario profile : benchmark.getProfiles()) {
            Histogram procTimeHistogram = emptyHistogram();
            Histogram recordTimeHistogram = emptyHistogram();
            Histogram recordsCallsCountHistogram = new Histogram(1, 2_000_000_000, 2);
            Histogram recordingsCountHistogram = new Histogram(1, 10_000_000, 2);

            for (int i = 0; i < ITERATIONS_PER_PROFILE; i++) {
                int recordsCount = run(benchmarkClazz, profile, procTimeHistogram, recordTimeHistogram, recordingsCountHistogram);
                recordsCallsCountHistogram.recordValue(recordsCount);
            }

            runResults.add(new BenchmarkRunResult(benchmarkClazz, profile, procTimeHistogram, recordTimeHistogram, recordsCallsCountHistogram, recordingsCountHistogram));
        }

        return runResults;
    }

    private static int run(
            Class<?> benchmarkClazz,
            BenchmarkScenario profile,
            Histogram procTimeHistogram,
            Histogram recordsTimeHistogram,
            Histogram recordingsCountHistogram) {

        try (TimeMeasurer $ = new TimeMeasurer(procTimeHistogram)) {

            BenchmarkProcessRunner.runClassInSeparateJavaProcess(benchmarkClazz, profile);
        }

        StorageReader reader = Optional.ofNullable(profile.getOutputFile()).map(OutputFile::toReader).orElse(StorageReader.empty());

        List<Recording> recordings = reader.getRecordings();
        recordingsCountHistogram.recordValue(recordings.size());

        if (!recordings.isEmpty()) {
            recordings.forEach(recording -> recordsTimeHistogram.recordValue(recording.getLifetime().toMillis()));
            return recordings.stream().mapToInt(Recording::callCount).sum();
        } else {
            recordsTimeHistogram.recordValue(0L);
            return 0;
        }
    }

    private static Histogram emptyHistogram() {
        return new Histogram(1, TimeUnit.MINUTES.toMillis(5), 2);
    }
}
