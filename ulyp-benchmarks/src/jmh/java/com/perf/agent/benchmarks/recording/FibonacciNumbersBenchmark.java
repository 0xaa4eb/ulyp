package com.perf.agent.benchmarks.recording;

import com.perf.agent.benchmarks.RecordingBenchmark;
import com.perf.agent.benchmarks.util.BenchmarkConstants;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime)
@Warmup(iterations = 20)
@Measurement(iterations = 30)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class FibonacciNumbersBenchmark extends RecordingBenchmark {

    private int compute(int x) {
        if (x < 0) {
            throw new IllegalArgumentException("Should be positive");
        }
        if (x == 1) {
            return 1;
        }
        if (x == 0) {
            return 1;
        }
        return compute(x - 2) + compute(x - 1);
    }

    @Fork(value = BenchmarkConstants.FORKS)
    @Benchmark
    public int computeBaseline() {
        return compute(18);
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.FibonacciNumbersBenchmark.sdjfhgsdhjfsd"
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public int computeInstrumented() {
        return compute(18);
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.FibonacciNumbersBenchmark.compute",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
            "-Dulyp.recording-queue.size=4194304"
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public int computeAndRecord() {
        // TODO direct mem limit reached with 31
        return compute(18);
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.FibonacciNumbersBenchmark.compute",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
            "-Dulyp.recording-queue.size=4194304"
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public int computeAndRecordSync(Counters counters) {
        // TODO direct mem limit reached with 31
        return execRecordAndSync(counters, () -> compute(18));
    }
}