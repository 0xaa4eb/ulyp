package com.perf.agent.benchmarks.recording;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Threads(4)
public class ConcurrentNumbersRecordingBenchmark extends NumbersRecordingBenchmark {

}