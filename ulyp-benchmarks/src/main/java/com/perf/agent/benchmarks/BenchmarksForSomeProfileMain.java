package com.perf.agent.benchmarks;

import com.perf.agent.benchmarks.impl.SpringHibernateSmallBenchmark;
import com.perf.agent.benchmarks.proc.BenchmarkProcessRunner;
import com.ulyp.core.CallEnterRecordList;
import com.ulyp.core.util.PackageList;
import com.ulyp.transport.TCallRecordLogUploadRequest;
import org.HdrHistogram.Histogram;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BenchmarksForSomeProfileMain {

    private static final int ITERATIONS_PER_PROFILE = 1;

    public static void main(String[] args) throws Exception {

        BenchmarkProfile trueProfile = new BenchmarkProfileBuilder()
                .withInstrumentedPackages(new PackageList("com", "org"))
//                .withAdditionalArgs(
//                        "-XX:+UnlockDiagnosticVMOptions",
//                        "-XX:+UnlockCommercialFeatures",
//                        "-XX:+FlightRecorder",
//                        "-XX:+DebugNonSafepoints",
//                        "-XX:StartFlightRecording=name=Profiling,dumponexit=true,delay=2s,filename=C:\\Temp\\myrecording.jfr,settings=profile"
//                )
                .build();

        for (PerformanceRunResult result : runBench(SpringHibernateSmallBenchmark.class, trueProfile)) {
            result.print();
        }
    }

    private static List<PerformanceRunResult> runBench(
            Class<? extends Benchmark> benchmarkClazz,
            BenchmarkProfile profile) throws Exception {
        List<PerformanceRunResult> runResults = new ArrayList<>();


        Histogram procTimeHistogram = emptyHistogram();
        Histogram recordingTimeHistogram = emptyHistogram();
        Histogram recordsCountHistogram = emptyHistogram();

        for (int i = 0; i < ITERATIONS_PER_PROFILE; i++) {
            int recordsCount = run(benchmarkClazz, profile, procTimeHistogram, recordingTimeHistogram);
            recordsCountHistogram.recordValue(recordsCount);
        }

        runResults.add(new PerformanceRunResult(benchmarkClazz, profile, procTimeHistogram, recordingTimeHistogram, recordsCountHistogram));

        return runResults;
    }

    private static int run(Class<?> benchmarkClazz, BenchmarkProfile profile, Histogram procTimeHistogram, Histogram recordingTimeHistogram) {

        try (MillisMeasured measured = new MillisMeasured(procTimeHistogram)) {
            BenchmarkProcessRunner.runClassInSeparateJavaProcess(benchmarkClazz, profile);

            if (profile.shouldWriteRecording()) {

                TCallRecordLogUploadRequest request = profile.getOutputFile().read().get(0);
                recordingTimeHistogram.recordValue(request.getRecordingInfo().getLifetimeMillis());

                CallEnterRecordList enterRecords = new CallEnterRecordList(request.getRecordLog().getEnterRecords());
                return enterRecords.size();
            }

            return 0;
        }
    }

    private static Histogram emptyHistogram() {
        return new Histogram(1, TimeUnit.MINUTES.toMillis(5), 2);
    }
}
