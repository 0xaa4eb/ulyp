package com.perf.agent.benchmarks.recording;

import com.perf.agent.benchmarks.RecordingBenchmark;
import com.perf.agent.benchmarks.util.BenchmarkConstants;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 20)
@Measurement(iterations = 30)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class StringsRecordingBenchmark extends RecordingBenchmark {

    @Param({"50000"})
    private int callCount;

    private String foo(String a, String b, String c, String d) {
        return String.valueOf(a.charAt(5)) + b.charAt(2) + c.charAt(6) + d.charAt(3) + "XVADASDASD";
    }

    @Fork(jvmArgs = "-Dulyp.off", value = BenchmarkConstants.FORKS)
    @Benchmark
    public String baseline() {
        return doCompute();
    }

    @Fork(jvmArgs = "-Dulyp.methods=**.StringsRecordingBenchmark.sdjfhgsdhjfsd", value = BenchmarkConstants.FORKS)
    @Benchmark
    public String instrumented() {
        return doCompute();
    }

    @Fork(jvmArgs = "-Dulyp.methods=**.StringsRecordingBenchmark.doCompute", value = BenchmarkConstants.FORKS)
    @Benchmark
    public String record() {
        return doCompute();
    }

    @Fork(jvmArgs = "-Dulyp.methods=**.StringsRecordingBenchmark.doCompute", value = BenchmarkConstants.FORKS)
    @Benchmark
    public String syncRecord(Counters counters) {
        return execSyncRecord(counters, this::doCompute);
    }

    private String doCompute() {
        String a = String.valueOf(ThreadLocalRandom.current().nextLong()) + "AJSHFGA^*U@";
        String b = String.valueOf(ThreadLocalRandom.current().nextLong()) + "MVJSGDHASDJ";
        String c = String.valueOf(ThreadLocalRandom.current().nextLong()) + "BKSJHDFJSDK";
        String d = String.valueOf(ThreadLocalRandom.current().nextLong()) + "BSKUJDFHSDF";
        for (int i = 0; i < callCount; i++) {
            d = foo(a, b, c, d);
        }
        return d;
    }
}