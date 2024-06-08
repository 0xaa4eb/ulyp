package com.perf.agent.benchmarks.recording.concurrent;

import com.perf.agent.benchmarks.recording.StringsRecordingTimeBenchmark;
import org.openjdk.jmh.annotations.*;

@Threads(4)
public class ConcurrentStringsRecordingTimeBenchmark extends StringsRecordingTimeBenchmark {

}
