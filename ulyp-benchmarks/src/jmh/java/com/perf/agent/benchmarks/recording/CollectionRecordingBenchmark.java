package com.perf.agent.benchmarks.recording;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import com.perf.agent.benchmarks.util.BenchmarkConstants;
import com.ulyp.agent.util.AgentHelper;

@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class CollectionRecordingBenchmark {

    @Param({"50000"})
    private int callCount;

    @Fork(value = 2)
    @BenchmarkMode(Mode.AverageTime)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public int computeBaseline() {
        return doCompute();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.CollectionRecordingBenchmark.sdjfhgsdhjfsd",
            "-Dulyp.collections=JAVA"
    }, value = 2)
    @BenchmarkMode(Mode.AverageTime)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public int computeInstrumented() {
        return doCompute();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.CollectionRecordingBenchmark.doCompute",
            "-Dulyp.collections=JAVA",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF"
    }, value = 2)
    @BenchmarkMode(Mode.AverageTime)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public int computeRecord() {
        return doCompute();
    }

    @Fork(jvmArgs = {
        BenchmarkConstants.AGENT_PROP,
        "-Dulyp.file=/tmp/test.dat",
        "-Dulyp.methods=**.CollectionRecordingBenchmark.doCompute",
        "-Dulyp.collections=JAVA",
        "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF"
    }, value = 2)
    @BenchmarkMode(Mode.AverageTime)
    @Benchmark
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public int computeRecordSync() throws InterruptedException, TimeoutException {
        int result = doCompute();
        AgentHelper.syncWriting();
        return result;
    }

    private List<Object> foo(List<String> a, List<Integer> b, List<Object> c) {
        if (a.size() >= 5) {
            a.remove(a.size() - 1);
        }
        if (b.size() >= 5) {
            b.remove(b.size() - 1);
        }

        List<Object> result = new ArrayList<>();
        result.add(a.get(0));
        result.add(b.get(0));
        result.add(c.get(0));

        a.add("ABC");
        b.add(ThreadLocalRandom.current().nextInt());

        return result;
    }

    private class X {

    }

    private int doCompute() {
        List<String> strings = new ArrayList<>();
        strings.add(String.valueOf(ThreadLocalRandom.current().nextInt()));
        strings.add(String.valueOf(ThreadLocalRandom.current().nextInt()));
        strings.add(String.valueOf(ThreadLocalRandom.current().nextInt()));
        strings.add(String.valueOf(ThreadLocalRandom.current().nextInt()));
        strings.add(String.valueOf(ThreadLocalRandom.current().nextInt()));

        List<Integer> ints = new ArrayList<>();
        ints.add(ThreadLocalRandom.current().nextInt());
        ints.add(ThreadLocalRandom.current().nextInt());
        ints.add(ThreadLocalRandom.current().nextInt());
        ints.add(ThreadLocalRandom.current().nextInt());
        ints.add(ThreadLocalRandom.current().nextInt());

        List<Object> objects = new ArrayList<>();
        objects.add(new X());
        objects.add(new X());
        objects.add(new X());
        objects.add(new X());

        int totalCount = 0;
        for (int i = 0; i < callCount; i++) {
            List<Object> result = foo(strings, ints, objects);
            totalCount += result.size();
        }
        return totalCount;
    }
}