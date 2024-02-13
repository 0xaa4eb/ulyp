package com.ulyp.agent.util;

import com.ulyp.core.metrics.*;
import com.ulyp.core.util.NamedThreadFactory;
import com.ulyp.core.util.SystemPropertyUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MetricDumper implements Closeable {

    private static final int METRIC_DUMP_INTERVAL_SECONDS = SystemPropertyUtil.getInt("ulyp.metrics.dump-interval.sec", 10);

    private final Metrics metrics;
    private final ScheduledExecutorService executor;

    public MetricDumper(Metrics metrics) {
        this.metrics = metrics;
        executor = Executors.newSingleThreadScheduledExecutor(
                NamedThreadFactory.builder()
                        .name("Ulyp-metrics-dumper")
                        .daemon(true)
                        .build()
        );
        executor.scheduleAtFixedRate(
                this::dumpAllMetrics,
                METRIC_DUMP_INTERVAL_SECONDS,
                METRIC_DUMP_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    private static class LogMetricSink implements MetricSink {

        @Override
        public void dump(Counter counter) {
            StringMetricOutput output = new StringMetricOutput();
            counter.dump(output);
            log.info(output.build());
        }

        @Override
        public void dump(BytesCounter bytesCounter) {
            StringMetricOutput output = new StringMetricOutput();
            bytesCounter.dump(output);
            log.info(output.build());
        }
    }

    private void dumpAllMetrics() {
        metrics.dump(new LogMetricSink());
    }

    public void close() throws RuntimeException {
        try {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
