package com.perf.agent.benchmarks.recording;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Threads(4)
public class ConcurrentStringsRecordingBenchmark extends StringsRecordingBenchmark {

}