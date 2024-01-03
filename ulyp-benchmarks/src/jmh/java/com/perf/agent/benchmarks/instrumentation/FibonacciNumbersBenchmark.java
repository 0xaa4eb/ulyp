package com.perf.agent.benchmarks.instrumentation;

import com.perf.agent.benchmarks.util.BenchmarkConstants;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class FibonacciNumbersBenchmark {

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

    @Fork(value = 2)
    @Benchmark
    public int computeBaseline() {
        return compute(18);
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.FibonacciNumbersBenchmark.sdjfhgsdhjfsd"
    }, value = 2)
    @Benchmark
    public int computeInstrumented() {
        return compute(18);
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.FibonacciNumbersBenchmark.compute",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF"
    }, value = 2)
    @Benchmark
    public int computeAndRecord() {
        // TODO direct mem limit reached with 31
        return compute(18);
    }
}