package com.perf.agent.benchmarks.recording;

import com.perf.agent.benchmarks.RecordingBenchmark;
import com.perf.agent.benchmarks.util.BenchmarkConstants;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime)
@Warmup(iterations = 20)
@Measurement(iterations = 50)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class FibonacciRecordingBenchmark extends RecordingBenchmark {

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

    @Fork(jvmArgs = "-Dulyp.off", value = BenchmarkConstants.FORKS)
    @Benchmark
    public int baseline() {
        return compute(27);
    }

    @Fork(jvmArgs = "-Dulyp.methods=**.FibonacciRecordingBenchmark.sdjfhgsdhjfsd", value = BenchmarkConstants.FORKS)
    @Benchmark
    public int instrumented() {
        return compute(27);
    }

    @Fork(jvmArgs = {
            "-Dulyp.methods=**.FibonacciRecordingBenchmark.compute",
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public int record() {
        // TODO direct mem limit reached with 31
        return compute(27);
    }

    @Fork(jvmArgs = "-Dulyp.methods=**.FibonacciRecordingBenchmark.compute", value = BenchmarkConstants.FORKS)
    @Benchmark
    public int syncRecord(Counters counters) {
        // TODO direct mem limit reached with 31
        return execSyncRecord(counters, () -> compute(27));
    }
}