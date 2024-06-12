package com.perf.agent.benchmarks.recording.concurrent;

import com.perf.agent.benchmarks.recording.CollectionRecordingBenchmark;
import org.openjdk.jmh.annotations.Threads;

@Threads(4)
public class ConcurrentCollectionRecordingBenchmark extends CollectionRecordingBenchmark {

}