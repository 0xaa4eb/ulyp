package com.ulyp.core.metrics;

public interface MetricOutput {

    MetricOutput write(String text);

    MetricOutput write(long value);

    MetricOutput write(double value);
}
