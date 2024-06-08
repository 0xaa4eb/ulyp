package com.perf.agent.benchmarks.recording.concurrent;

import com.perf.agent.benchmarks.recording.NumbersRecordingTimeBenchmark;
import org.openjdk.jmh.annotations.*;

@Threads(4)
public class ConcurrentNumbersRecordingTimeBenchmark extends NumbersRecordingTimeBenchmark {

}