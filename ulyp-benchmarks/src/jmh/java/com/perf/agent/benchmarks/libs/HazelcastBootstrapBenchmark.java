package com.perf.agent.benchmarks.libs;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.perf.agent.benchmarks.util.BenchmarkConstants;
import com.ulyp.agent.util.AgentHelper;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
public class HazelcastBootstrapBenchmark {

    @Fork(value = 2)
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
    }, value = 2)
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
    }, value = 2)
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
    }, value = 2)
    @Benchmark
    public void bootstrapRecordSync() throws InterruptedException, TimeoutException {
        run();
        AgentHelper.syncWriting();
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
