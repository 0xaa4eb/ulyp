package com.perf.agent.benchmarks.recording;

import com.perf.agent.benchmarks.RecordingBenchmark;
import com.perf.agent.benchmarks.util.BenchmarkConstants;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class NumbersRecordingBenchmark extends RecordingBenchmark {

    @Param({"50000"})
    private int callCount;

    private long foo(long x, long y, long z, double v, double b) {
        return x + y + z;
    }

    @Fork(value = 2)
    @BenchmarkMode(Mode.AverageTime)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public long computeBaseline() {
        return doCompute();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.NumbersRecordingBenchmark.sdjfhgsdhjfsd"
    }, value = 2)
    @BenchmarkMode(Mode.AverageTime)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public long computeInstrumented() {
        return doCompute();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.NumbersRecordingBenchmark.doCompute",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF"
    }, value = 2)
    @BenchmarkMode(Mode.AverageTime)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public long computeRecord() {
        return doCompute();
    }

    @Fork(jvmArgs = {
        BenchmarkConstants.AGENT_PROP,
        "-Dulyp.file=/tmp/test.dat",
        "-Dulyp.methods=**.NumbersRecordingBenchmark.doCompute",
        "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF"
    }, value = 2)
    @BenchmarkMode(Mode.AverageTime)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public long computeRecordSync(Counters counters) {
        return execRecordAndSync(counters, this::doCompute);
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