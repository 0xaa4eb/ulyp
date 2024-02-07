package com.perf.agent.benchmarks;

import com.ulyp.agent.util.AgentHelper;
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
    }

    public <T> void execRecordAndSync(Counters counters, Runnable runnable) {
        long bytes = AgentHelper.estimateBytesWritten();
        runnable.run();
        AgentHelper.syncWriting();
        counters.bytesWritten = AgentHelper.estimateBytesWritten() - bytes;
        System.out.println(counters.bytesWritten);
    }

    public <T> T execRecordAndSync(Counters counters, Supplier<T> supplier) {
        long bytes = AgentHelper.estimateBytesWritten();
        T result = supplier.get();
        AgentHelper.syncWriting();
        counters.bytesWritten = AgentHelper.estimateBytesWritten() - bytes;
        return result;
    }
}
