package com.perf.agent.benchmarks;

import com.ulyp.agent.util.AgentHelper;
import org.openjdk.jmh.annotations.*;

import java.util.function.Supplier;

@State(Scope.Benchmark)
public class RecordingBenchmark {

    @State(Scope.Thread)
    @AuxCounters(AuxCounters.Type.EVENTS)
    public static class Counters {
        public long bytesWritten;
    }

    /**
     * Simple sleep as the relaxation is added to all benchmarks. The main purpose is to enforce a pattern
     * where burst of methods (usually like ~200k-300k) is recorded with a small pause going afterwards. This
     * closely resembles the real usage of Ulyp in enterprise apps. We use sync writing (wait until all data is flushed)
     * and then we insert a small pause.
     */
    @TearDown(Level.Invocation)
    public void relax() {
        AgentHelper.syncWriting();

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void execRecordAndSync(Counters counters, Runnable runnable) {
        long bytes = AgentHelper.estimateBytesWritten();
        runnable.run();
        AgentHelper.syncWriting();
        counters.bytesWritten = AgentHelper.estimateBytesWritten() - bytes;
    }

    public <T> T execRecordAndSync(Counters counters, Supplier<T> supplier) {
        long bytes = AgentHelper.estimateBytesWritten();
        T result = supplier.get();
        AgentHelper.syncWriting();
        counters.bytesWritten = AgentHelper.estimateBytesWritten() - bytes;
        return result;
    }
}
