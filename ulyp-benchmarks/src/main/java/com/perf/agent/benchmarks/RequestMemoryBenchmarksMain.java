package com.perf.agent.benchmarks;

import com.perf.agent.benchmarks.impl.H2MemDatabaseBenchmark;
import com.perf.agent.benchmarks.impl.SpringHibernateSmallBenchmark;
import com.perf.agent.benchmarks.proc.BenchmarkProcessRunner;
import com.ulyp.transport.TCallRecordLogUploadRequest;
import org.HdrHistogram.Histogram;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RequestMemoryBenchmarksMain {

    public static void main(String[] args) throws Exception {

        List<RequestMemoryRunResult> runResults = new ArrayList<>();

        runResults.addAll(runBench(H2MemDatabaseBenchmark.class));
        runResults.addAll(runBench(SpringHibernateSmallBenchmark.class));

        for (RequestMemoryRunResult runResult : runResults) {
            System.out.println(runResult);
        }
    }

    private static List<RequestMemoryRunResult> runBench(Class<? extends Benchmark> benchmarkClazz) throws Exception {
        List<RequestMemoryRunResult> runResults = new ArrayList<>();

        Benchmark benchmark = benchmarkClazz.newInstance();

        for (BenchmarkProfile profile : benchmark.getProfiles()) {
            if (!profile.shouldWriteRecording()) {
                // If nothing is sent to UI, then there is nothing to measure
                continue;
            }

            TCallRecordLogUploadRequest request = run(benchmarkClazz, profile);
            int byteSize = request.getSerializedSize();
            runResults.add(new RequestMemoryRunResult(benchmarkClazz, profile, byteSize));
        }

        return runResults;
    }

    private static TCallRecordLogUploadRequest run(Class<?> benchmarkClazz, BenchmarkProfile profile) {
        BenchmarkProcessRunner.runClassInSeparateJavaProcess(benchmarkClazz, profile);

        return profile.getOutputFile().read().get(0);
    }
}
