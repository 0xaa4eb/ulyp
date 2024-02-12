package com.perf.agent.benchmarks.recording;

import com.perf.agent.benchmarks.RecordingBenchmark;
import com.perf.agent.benchmarks.util.BenchmarkConstants;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@State(Scope.Benchmark)
@Warmup(iterations = 20)
@Measurement(iterations = 60)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class StringsRecordingBenchmark extends RecordingBenchmark {

    @Param({"50000"})
    private int callCount;

    private String foo(String a, String b, String c, String d) {
        return String.valueOf(a.charAt(5)) + b.charAt(2) + c.charAt(6) + d.charAt(3) + "XVADASDASD";
    }

    @Fork(value = BenchmarkConstants.FORKS)
    @Benchmark
    public String computeBaseline() {
        return doCompute();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.StringsRecordingBenchmark.sdjfhgsdhjfsd"
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public String computeInstrumented() {
        return doCompute();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.StringsRecordingBenchmark.doCompute",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF"
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public String computeRecord() {
        return doCompute();
    }

    @Fork(jvmArgs = {
        BenchmarkConstants.AGENT_PROP,
        "-Dulyp.file=/tmp/test.dat",
        "-Dulyp.methods=**.StringsRecordingBenchmark.doCompute",
        "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF"
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public String computeRecordSync(Counters counters) throws InterruptedException, TimeoutException {
        return execRecordAndSync(counters, this::doCompute);
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