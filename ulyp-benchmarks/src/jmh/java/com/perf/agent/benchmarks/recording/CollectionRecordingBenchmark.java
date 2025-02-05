package com.perf.agent.benchmarks.recording;

import com.perf.agent.benchmarks.RecordingBenchmark;
import com.perf.agent.benchmarks.util.BenchmarkConstants;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@Warmup(iterations = 20)
@Measurement(iterations = 30)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class CollectionRecordingBenchmark extends RecordingBenchmark {

    @Param({"250000"})
    private int callCount;

    @Fork(jvmArgs = "-Dulyp.off", value = BenchmarkConstants.FORKS)
    @Benchmark
    public int baseline() {
        return doCompute();
    }

    @Fork(jvmArgs = "-Dulyp.methods=**.CollectionRecordingBenchmark.sdjfhgsdhjfsd", value = BenchmarkConstants.FORKS)
    @Benchmark
    public int instrumented() {
        return doCompute();
    }

    @Fork(jvmArgs = "-Dulyp.methods=**.CollectionRecordingBenchmark.doCompute", value = BenchmarkConstants.FORKS)
    @Benchmark
    public int record() {
        return doCompute();
    }

    @Fork(jvmArgs = "-Dulyp.methods=**.CollectionRecordingBenchmark.doCompute", value = BenchmarkConstants.FORKS)
    @Benchmark
    public int syncRecord(Counters counters) throws InterruptedException {
        return execSyncRecord(counters, this::doCompute);
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

    private Integer doCompute() {
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