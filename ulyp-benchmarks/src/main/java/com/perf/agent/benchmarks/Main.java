package com.perf.agent.benchmarks;

import com.perf.agent.benchmarks.benchmarks.*;
import com.perf.agent.benchmarks.proc.BenchmarkProcessRunner;
import com.perf.agent.benchmarks.proc.OutputFile;
import com.ulyp.storage.tree.Recording;
import com.ulyp.storage.RecordingDataReader;
import org.HdrHistogram.Histogram;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final int ITERATIONS_PER_PROFILE = 2;

    public static void main(String[] args) throws Exception {

        List<BenchmarkRunResult> runResults = new ArrayList<>();

        runResults.addAll(runBenchmark(H2MemDatabaseBenchmark.class));
        runResults.addAll(runBenchmark(ActivemqBenchmark.class));
        runResults.addAll(runBenchmark(FibonacciNumbersBenchmark.class));
//        runResults.addAll(runBenchmark(SpringHibernateSmallBenchmark.class));
        runResults.addAll(runBenchmark(SpringHibernateMediumBenchmark.class));

        for (BenchmarkRunResult runResult : runResults) {
            runResult.print();
        }
    }

    private static List<BenchmarkRunResult> runBenchmark(Class<? extends Benchmark> benchmarkClazz) throws Exception {
        List<BenchmarkRunResult> runResults = new ArrayList<>();
        Benchmark benchmark = benchmarkClazz.newInstance();

        for (BenchmarkScenario scenario : benchmark.getProfiles()) {
            runResults.add(runScenario(benchmarkClazz, scenario));
        }

        return runResults;
    }

    private static BenchmarkRunResult runScenario(Class<? extends Benchmark> benchmarkClazz, BenchmarkScenario scenario) {
        Histogram procRunTimeHistogram = emptyHistogram();
        Histogram recordTimeHistogram = emptyHistogram();
        Histogram recordsCallsCountHistogram = new Histogram(1, 2_000_000_000, 2);
        Histogram recordingsCountHistogram = new Histogram(1, 10_000_000, 2);
        Histogram outputFileSizeHistogram = new Histogram(1, 2_000_000_000, 2);

        for (int i = 0; i < ITERATIONS_PER_PROFILE; i++) {
            OutputFile outputFile = run(benchmarkClazz, scenario, procRunTimeHistogram);

            RecordingDataReader recordingDataReader = Optional.ofNullable(outputFile).map(OutputFile::toReader).orElse(RecordingDataReader.empty());

            List<Recording> recordings = recordingDataReader.getRecordings();
            recordingsCountHistogram.recordValue(recordings.size());
            outputFileSizeHistogram.recordValue(Optional.ofNullable(outputFile).map(OutputFile::size).orElse(0L));

            if (!recordings.isEmpty()) {
                recordings.forEach(recording -> recordTimeHistogram.recordValue(recording.getLifetime().toMillis()));
                recordsCallsCountHistogram.recordValue(recordings.stream().mapToInt(Recording::callCount).sum());
            } else {
                recordTimeHistogram.recordValue(0L);
                recordsCallsCountHistogram.recordValue(0);
            }
        }

        return new BenchmarkRunResult(benchmarkClazz, scenario, procRunTimeHistogram, recordTimeHistogram, recordsCallsCountHistogram, recordingsCountHistogram, outputFileSizeHistogram);
    }

    private static OutputFile run(
            Class<?> benchmarkClazz,
            BenchmarkScenario profile,
            Histogram procTimeHistogram) {

        try (TimeMeasurer $ = new TimeMeasurer(procTimeHistogram)) {

            BenchmarkProcessRunner.runClassInSeparateJavaProcess(benchmarkClazz, profile);
        }
        return profile.getOutputFile();
    }

    private static Histogram emptyHistogram() {
        return new Histogram(1, TimeUnit.MINUTES.toMillis(5), 2);
    }
}
