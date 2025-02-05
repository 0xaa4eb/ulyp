package com.perf.agent.benchmarks.recording;

import com.perf.agent.benchmarks.RecordingBenchmark;
import com.perf.agent.benchmarks.util.BenchmarkConstants;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime)
@State(Scope.Benchmark)
@Warmup(iterations = 20)
@Measurement(iterations = 30)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class NumbersRecordingBenchmark extends RecordingBenchmark {

    @Param({"250000"})
    private int callCount;

    private long foo(long x, long y, long z, double v, double b) {
        return x + y + z;
    }

    @Fork(jvmArgs = "-Dulyp.off", value = BenchmarkConstants.FORKS)
    @Benchmark
    public long baseline() {
        return doCompute();
    }

    @Fork(jvmArgs = "-Dulyp.methods=**.NumbersRecordingBenchmark.sdjfhgsdhjfsd", value = BenchmarkConstants.FORKS)
    @Benchmark
    public long instrumented() {
        return doCompute();
    }

    @Fork(jvmArgs = {
            "-Dulyp.methods=**.NumbersRecordingBenchmark.doCompute",
            "-Dulyp.metrics",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=INFO",
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public long record() {
        return doCompute();
    }

    @Fork(jvmArgs = {
        "-Dulyp.methods=**.NumbersRecordingBenchmark.doCompute",
        "-Dulyp.metrics",
        "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public long syncRecord(Counters counters) {
        return execSyncRecord(counters, this::doCompute);
    }

    private Long doCompute() {
        long res = 0;
        for (int i = 0; i < callCount; i++) {
            res = (res - 1) ^ foo(
                    ThreadLocalRandom.current().nextInt(),
                    ThreadLocalRandom.current().nextInt(),
                    ThreadLocalRandom.current().nextInt(),
                    ThreadLocalRandom.current().nextDouble(),
                    ThreadLocalRandom.current().nextDouble());
        }
        return res;
    }
}