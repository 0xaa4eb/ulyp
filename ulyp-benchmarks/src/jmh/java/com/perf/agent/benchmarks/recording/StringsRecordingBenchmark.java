package com.perf.agent.benchmarks.recording;

import com.perf.agent.benchmarks.util.BenchmarkConstants;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class StringsRecordingBenchmark {

    @Param({"50000"})
    private int callCount;

    private String foo(String a, String b, String c, String d) {
        return String.valueOf(a.charAt(5)) + b.charAt(2) + c.charAt(6) + d.charAt(3) + "XVADASDASD";
    }

    @Fork(value = 2)
    @BenchmarkMode(Mode.AverageTime)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public String computeBaseline() {
        return compute();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.StringsRecordingBenchmark.sdjfhgsdhjfsd"
    }, value = 2)
    @BenchmarkMode(Mode.AverageTime)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public String computeInstrumented() {
        return compute();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.StringsRecordingBenchmark.computeRecord",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF"
    }, value = 2)
    @BenchmarkMode(Mode.AverageTime)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public String computeRecord() {
        return compute();
    }

    private String compute() {
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