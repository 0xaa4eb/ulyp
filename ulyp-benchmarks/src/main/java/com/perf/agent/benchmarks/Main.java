package com.perf.agent.benchmarks;

import com.perf.agent.benchmarks.instrumentation.InstrumentationBenchmark;
import com.perf.agent.benchmarks.proc.BenchmarkProcessRunner;
import com.perf.agent.benchmarks.proc.OutputFile;
import com.perf.agent.benchmarks.proc.RecordingResult;
import com.perf.agent.benchmarks.recording.ActivemqBenchmark;
import com.perf.agent.benchmarks.recording.FibonacciNumbersBenchmark;
import com.perf.agent.benchmarks.recording.H2MemDatabaseBenchmark;
import com.perf.agent.benchmarks.recording.SpringHibernateMediumBenchmark;
import com.ulyp.core.RecordingMetadata;

import org.HdrHistogram.Histogram;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final int ITERATIONS_PER_PROFILE = 5;

    public static void main(String[] args) throws Exception {

        List<BenchmarkRunResult> runResults = new ArrayList<>();

        runResults.addAll(runBenchmark(H2MemDatabaseBenchmark.class));
        runResults.addAll(runBenchmark(ActivemqBenchmark.class));
        runResults.addAll(runBenchmark(FibonacciNumbersBenchmark.class));
        runResults.addAll(runBenchmark(SpringHibernateMediumBenchmark.class));
        runResults.addAll(runBenchmark(InstrumentationBenchmark.class));

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

    private static BenchmarkRunResult runScenario(Class<? extends Benchmark> benchmarkClazz, BenchmarkScenario scenario) throws InterruptedException {
        Histogram procRunTimeHistogram = emptyHistogram();
        Histogram recordTimeHistogram = emptyHistogram();
        Histogram recordsCallsCountHistogram = new Histogram(1, 2_000_000_000, 2);
        Histogram recordingsCountHistogram = new Histogram(1, 10_000_000, 2);
        Histogram outputFileSizeHistogram = new Histogram(1, 2_000_000_000, 2);

        for (int i = 0; i < ITERATIONS_PER_PROFILE; i++) {
            OutputFile outputFile = run(benchmarkClazz, scenario, procRunTimeHistogram);

            if (outputFile != null) {
                RecordingResult recordingResult = outputFile.toRecordingResult();

                recordingsCountHistogram.recordValue(recordingResult.getRecordingsCount());
                outputFileSizeHistogram.recordValue(outputFile.size());

                Collection<RecordingMetadata> recordingMetadataMap = recordingResult.getRecordingMetadataMap();

                if (!recordingMetadataMap.isEmpty()) {
                    int calls = 0;
                    for (RecordingMetadata recordingMetadata : recordingMetadataMap) {
                        recordTimeHistogram.recordValue(recordingMetadata.getRecordingCompletedEpochMillis() - recordingMetadata.getRecordingStartedEpochMillis());
                        calls += recordingResult.getRecordedCalls(recordingMetadata.getId());
                    }
                    recordsCallsCountHistogram.recordValue(calls);
                } else {
                    recordTimeHistogram.recordValue(0L);
                    recordsCallsCountHistogram.recordValue(0);
                }
            } else {
                recordingsCountHistogram.recordValue(0);
                outputFileSizeHistogram.recordValue(0);
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
