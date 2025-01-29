package com.perf.agent.benchmarks;

import com.perf.agent.benchmarks.util.BenchmarkConstants;
import com.ulyp.agent.AgentContext;
import com.ulyp.agent.util.AgentHelper;
import com.ulyp.core.metrics.Counter;
import org.openjdk.jmh.annotations.*;

import java.util.function.Supplier;

@Fork(jvmArgsPrepend = {
        "-Xms6G",
        "-Xmx6G",
        "-XX:+AlwaysPreTouch",
        BenchmarkConstants.ENABLE_AGENT_SYSTEM_PROP,
        "-Dulyp.file=/tmp/test.dat",
        "-Dulyp.record-constructors",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED"
})
@State(Scope.Benchmark)
public class RecordingBenchmark {

    // additional non-existent methods to make instrumentation matcher do more work
    protected static final String METHOD_MATCHERS =
            "org.apache.activemq.JMSMssfasdasfa.kdusdhfe," +
            "org.apache.activemq.ActiveMQInstrumentation.vnxmxhcs," +
            "org.apache.activemq.Ajaxcas.dfksdjf," +
            "**.test.JMSMessage.fsdjkhgsd," +
            "**.ActiveMQBootstrapBenchmark.xvxcxxx";

    @State(Scope.Thread)
    @AuxCounters(AuxCounters.Type.EVENTS)
    public static class Counters {
        public long bytesWritten;
        public long recordingQueueStalls;
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

    public <T> void execSyncRecord(Counters counters, Runnable runnable) {
        Counter stallsCounter = AgentContext.getCtx().getMetrics().getOrCreateCounter("recording.queue.stalls");
        long stalls = stallsCounter.getValue();
        long bytes = AgentHelper.estimateBytesWritten();
        runnable.run();
        AgentHelper.syncWriting();
        counters.bytesWritten = AgentHelper.estimateBytesWritten() - bytes;
        counters.recordingQueueStalls = stallsCounter.getValue() - stalls;
    }

    public <T> T execSyncRecord(Counters counters, Supplier<T> supplier) {
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
