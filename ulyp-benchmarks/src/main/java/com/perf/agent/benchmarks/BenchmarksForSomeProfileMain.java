package com.perf.agent.benchmarks;

import com.perf.agent.benchmarks.benchmarks.FibonacciNumbersBenchmark;
import com.perf.agent.benchmarks.proc.BenchmarkProcessRunner;
import com.perf.agent.benchmarks.proc.OutputFile;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.Recording;
import com.ulyp.storage.StorageReader;
import org.HdrHistogram.Histogram;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class BenchmarksForSomeProfileMain {

    private static final int ITERATIONS_PER_PROFILE = 1;

    public static void main(String[] args) {

        BenchmarkScenario trueProfile = new BenchmarkScenarioBuilder()
                .withMethodToRecord(new MethodMatcher(FibonacciNumbersBenchmark.class, "asdsad"))
                .withAdditionalArgs("-Dnum=42")
/*                .withAdditionalArgs(
                        "-XX:+UnlockDiagnosticVMOptions",
                        "-XX:+UnlockCommercialFeatures",
                        "-XX:+FlightRecorder",
                        "-Dnum=50",
                        "-XX:+DebugNonSafepoints",
                        "-XX:StartFlightRecording=name=Profiling,duration=30s,delay=1s,filename=C:\\Temp\\myrecording.jfr,settings=profile"
                )*/
                .build();

        for (BenchmarkRunResult result : runBench(FibonacciNumbersBenchmark.class, trueProfile)) {
            result.print();
        }
    }

    private static List<BenchmarkRunResult> runBench(Class<? extends Benchmark> benchmarkClazz, BenchmarkScenario profile) {
        List<BenchmarkRunResult> runResults = new ArrayList<>();

        Histogram procTimeHistogram = emptyHistogram();
        Histogram recordingTimeHistogram = emptyHistogram();
        Histogram recordsCallsCountHistogram = new Histogram(1, 2_000_000_000, 2);
        Histogram recordingsCountHistogram = new Histogram(1, 10_000_000, 2);

        for (int i = 0; i < ITERATIONS_PER_PROFILE; i++) {
            int callsCount = run(benchmarkClazz, profile, procTimeHistogram, recordingTimeHistogram, recordingsCountHistogram);
            recordsCallsCountHistogram.recordValue(callsCount);
        }

        runResults.add(new BenchmarkRunResult(benchmarkClazz, profile, procTimeHistogram, recordingTimeHistogram, recordsCallsCountHistogram, recordingsCountHistogram));

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
