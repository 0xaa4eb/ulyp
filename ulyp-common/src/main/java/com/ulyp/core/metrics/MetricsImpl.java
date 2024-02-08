package com.ulyp.core.metrics;

import java.util.LinkedHashMap;
import java.util.Map;

public class MetricsImpl implements Metrics {

    private final Map<String, Counter> counters = new LinkedHashMap<>();
    private final Map<String, BytesCounter> bytesCounters = new LinkedHashMap<>();

    @Override
    public synchronized Counter getOrCreateCounter(String name) {
        return counters.computeIfAbsent(name, CounterImpl::new);
    }

    @Override
    public synchronized BytesCounter getOrCreateByteCounter(String name) {
        return bytesCounters.computeIfAbsent(name, BytesCounterImpl::new);
    }

    @Override
    public synchronized void dump(MetricSink sink) {
        for (Map.Entry<String, Counter> counter : counters.entrySet()) {
            sink.dump(counter.getValue());
        }
        for (Map.Entry<String, BytesCounter> counter : bytesCounters.entrySet()) {
            sink.dump(counter.getValue());
        }
    }
}
