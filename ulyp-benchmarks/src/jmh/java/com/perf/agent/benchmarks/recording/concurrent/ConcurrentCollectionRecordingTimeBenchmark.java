package com.perf.agent.benchmarks.recording.concurrent;

import com.perf.agent.benchmarks.recording.CollectionRecordingTimeBenchmark;
import org.openjdk.jmh.annotations.Threads;

@Threads(4)
public class ConcurrentCollectionRecordingTimeBenchmark extends CollectionRecordingTimeBenchmark {

}