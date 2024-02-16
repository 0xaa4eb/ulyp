package com.perf.agent.benchmarks.recording.concurrent;

import com.perf.agent.benchmarks.recording.StringsRecordingBenchmark;
import org.openjdk.jmh.annotations.*;

@Threads(4)
public class ConcurrentStringsRecordingBenchmark extends StringsRecordingBenchmark {

}
