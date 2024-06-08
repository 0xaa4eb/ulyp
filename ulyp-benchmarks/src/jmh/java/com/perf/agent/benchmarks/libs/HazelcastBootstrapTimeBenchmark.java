package com.perf.agent.benchmarks.libs;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.perf.agent.benchmarks.RecordingTimeBenchmark;
import org.openjdk.jmh.annotations.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
public class HazelcastBootstrapTimeBenchmark extends RecordingTimeBenchmark {

    @Fork(jvmArgs = "-Dulyp.off", value = 3)
    @Benchmark
    public void baseline() {
        run();
    }

    @Fork(jvmArgs = "-Dulyp.methods=" + METHOD_MATCHERS + ",**.HazelcastBootstrapBenchmark.xcjznfgasd", value = 3)
    @Benchmark
    public void instrumented() {
        run();
    }

    @Fork(jvmArgs = "-Dulyp.methods=" + METHOD_MATCHERS + ", **.HazelcastBootstrapBenchmark.run", value = 3)
    @Benchmark
    public void record() {
        run();
    }

    @Fork(jvmArgs = "-Dulyp.methods=" + METHOD_MATCHERS + ",**.HazelcastBootstrapBenchmark.run", value = 3)
    @Benchmark
    public void syncRecord(Counters counters) {
        execSyncRecord(counters, this::run);
    }

    private void run() {
        Config helloWorldConfig = new Config();
        helloWorldConfig.setClusterName("bench-cluster");

        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(helloWorldConfig);
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(helloWorldConfig);

        Map<String, String> map1 = instance1.getMap("my-distributed-map");
        Map<String, String> map2 = instance2.getMap("my-distributed-map");

        for (int i = 0; i < 1000; i++) {
            map1.put(String.valueOf(i), "Value" + i);
        }

        instance1.shutdown();
        instance2.shutdown();
    }
}
