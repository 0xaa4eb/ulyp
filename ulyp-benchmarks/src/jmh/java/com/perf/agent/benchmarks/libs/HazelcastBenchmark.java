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
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 20)
@Measurement(iterations = 50)
public class HazelcastBenchmark extends RecordingBenchmark {

    public static final int PUTS_PER_INVOCATION = 100;
    public static final int KEYS_COUNT = 50000;

    private HazelcastInstance instance1;
    private HazelcastInstance instance2;
    private Map<String, String> map1;
    private Map<String, String> map2;
    private int opIndex = 0;

    @Setup(Level.Trial)
    public void setup() {
        Config helloWorldConfig = new Config();
        helloWorldConfig.setClusterName("bench-cluster");

        instance1 = Hazelcast.newHazelcastInstance(helloWorldConfig);
        instance2 = Hazelcast.newHazelcastInstance(helloWorldConfig);

        map1 = instance1.getMap("my-distributed-map");
        map2 = instance2.getMap("my-distributed-map");
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        if (map2.isEmpty()) {
            throw new RuntimeException("Test failed");
        }
        instance1.shutdown();
        instance2.shutdown();
    }

    @Fork(value = BenchmarkConstants.FORKS)
    @Benchmark
    public void putBaseline() {
        put();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.HazelcastBenchmark.zxc",
            "-Dulyp.constructors",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public void sendMsgInstrumented() {
        put();
    }


    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.HazelcastBenchmark.put",
            "-Dulyp.constructors",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public void sendMsgRecord() {
        put();
    }

    @Fork(jvmArgs = {
        BenchmarkConstants.AGENT_PROP,
        "-Dulyp.file=/tmp/test.dat",
        "-Dulyp.methods=**.HazelcastBenchmark.put",
        "-Dulyp.constructors",
        "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public void putRecordSync(Counters counters) {
        execRecordAndSync(counters, this::put);
    }

    private void put() {
        for (int i = 0; i < PUTS_PER_INVOCATION; i++) {
            int index = opIndex++;
            if (index >= KEYS_COUNT) {
                opIndex = 0;
            }
            map1.put(String.valueOf(index), "Value" + index);
        }
    }
}
