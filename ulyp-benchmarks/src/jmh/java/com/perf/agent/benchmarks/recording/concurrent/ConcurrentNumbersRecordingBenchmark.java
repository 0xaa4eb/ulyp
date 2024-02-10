package com.perf.agent.benchmarks.recording.concurrent;

import com.perf.agent.benchmarks.recording.NumbersRecordingBenchmark;
import org.openjdk.jmh.annotations.*;

@Threads(4)
public class ConcurrentNumbersRecordingBenchmark extends NumbersRecordingBenchmark {

}