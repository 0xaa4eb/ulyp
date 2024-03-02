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
public class BooleanRecordingBenchmark extends RecordingBenchmark {

    @Param({"250000"})
    private int callCount;

    private int foo(boolean x, boolean y, boolean z) {
        return x ^ y ^ z ? 1 : 0;
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.BooleanRecordingBenchmark.doCompute",
            "-Dulyp.metrics",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=INFO",
            "-Dulyp.recording-queue.size=4194304"
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public long record() {
        return doCompute();
    }

    private Long doCompute() {
        long res = 0;
        for (int i = 0; i < callCount; i++) {
            res = (res - 1) ^ foo(
                    ThreadLocalRandom.current().nextBoolean(),
                    ThreadLocalRandom.current().nextBoolean(),
                    ThreadLocalRandom.current().nextBoolean()
            );
        }
        return res;
    }
}