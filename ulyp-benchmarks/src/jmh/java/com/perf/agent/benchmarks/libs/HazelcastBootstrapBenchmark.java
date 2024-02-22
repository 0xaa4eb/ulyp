package com.perf.agent.benchmarks.libs;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.perf.agent.benchmarks.RecordingBenchmark;
import com.perf.agent.benchmarks.util.BenchmarkConstants;
import org.openjdk.jmh.annotations.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
public class HazelcastBootstrapBenchmark extends RecordingBenchmark {

    @Fork(value = BenchmarkConstants.FORKS)
    @Benchmark
    public void bootstrapBaseline() {
        run();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.HazelcastBootstrapBenchmark.xcjznfgasd",
            "-Dulyp.constructors",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public void bootstrapInstrumented() {
        run();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.HazelcastBootstrapBenchmark.run",
            "-Dulyp.constructors",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public void bootstrapRecord() {
        run();
    }

    @Fork(jvmArgs = {
        BenchmarkConstants.AGENT_PROP,
        "-Dulyp.file=/tmp/test.dat",
        "-Dulyp.methods=**.HazelcastBootstrapBenchmark.run",
        "-Dulyp.constructors",
        "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public void bootstrapRecordSync(Counters counters) {
        execRecordAndSync(counters, this::run);
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
