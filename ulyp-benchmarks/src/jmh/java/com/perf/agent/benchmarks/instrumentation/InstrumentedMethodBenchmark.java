package com.perf.agent.benchmarks.instrumentation;

import com.perf.agent.benchmarks.util.BenchmarkConstants;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class InstrumentedMethodBenchmark {

    @State(Scope.Benchmark)
    public static class State1 {
        public int field;

        @Setup(Level.Iteration)
        public void setup() {
            field = ThreadLocalRandom.current().nextInt();
        }
    }

    @State(Scope.Benchmark)
    public static class State2 {
        public int field;

        @Setup(Level.Iteration)
        public void setup() {
            field = ThreadLocalRandom.current().nextInt();
        }
    }

    @State(Scope.Benchmark)
    public static class State3 {
        public int field;

        @Setup(Level.Iteration)
        public void setup() {
            field = ThreadLocalRandom.current().nextInt();
        }
    }

    @Fork(value = BenchmarkConstants.FORKS)
    @Benchmark
    public String returnObjectBaseline(State1 s1, State2 s2, State3 s3) {
        return "ABC";
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.ENABLE_AGENT_SYSTEM_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.InstrumentedMethodBenchmark.c",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
            "-Dulyp.constructors"
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public String returnObjectInstrumented(State1 s1, State2 s2, State3 s3) {
        return "ABC";
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.ENABLE_AGENT_SYSTEM_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.InstrumentedMethodBenchmark.c",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
            "-Dulyp.constructors"
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    @CompilerControl(CompilerControl.Mode.EXCLUDE)
    public String returnObjectNoCompile(State1 s1, State2 s2, State3 s3) {
        return "ABC";
    }
}
