package com.perf.agent.benchmarks;

import com.ulyp.agent.AgentContext;
import com.ulyp.agent.util.AgentHelper;
import com.ulyp.core.metrics.Counter;
import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.function.Supplier;

@State(Scope.Benchmark)
public class RecordingBenchmark {

    @State(Scope.Thread)
    @AuxCounters(AuxCounters.Type.EVENTS)
    public static class Counters {
        public long bytesWritten;
        public long recordingQueueStalls;
    }

    public <T> void execRecordAndSync(Counters counters, Runnable runnable) {
        Counter stallsCounter = AgentContext.getCtx().getMetrics().getOrCreateCounter("recording.queue.stalls");
        long stalls = stallsCounter.getValue();
        long bytes = AgentHelper.estimateBytesWritten();
        runnable.run();
        AgentHelper.syncWriting();
        counters.bytesWritten = AgentHelper.estimateBytesWritten() - bytes;
        counters.recordingQueueStalls = stallsCounter.getValue() - stalls;
    }

    public <T> T execRecordAndSync(Counters counters, Supplier<T> supplier) {
        Counter stallsCounter = AgentContext.getCtx().getMetrics().getOrCreateCounter("recording.queue.stalls");
        long stalls = stallsCounter.getValue();
        long bytes = AgentHelper.estimateBytesWritten();
        T result = supplier.get();
        AgentHelper.syncWriting();
        counters.bytesWritten = AgentHelper.estimateBytesWritten() - bytes;
        counters.recordingQueueStalls = stallsCounter.getValue() - stalls;
        return result;
    }
}
