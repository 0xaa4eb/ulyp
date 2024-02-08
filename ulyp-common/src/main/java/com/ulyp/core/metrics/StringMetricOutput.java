package com.ulyp.core.metrics;

public class StringMetricOutput implements MetricOutput {

    private final StringBuilder out;

    public StringMetricOutput() {
        this.out = new StringBuilder();
    }

    @Override
    public MetricOutput write(String text) {
        this.out.append(text);
        return this;
    }

    @Override
    public MetricOutput write(long value) {
        this.out.append(value);
        return this;
    }

    @Override
    public MetricOutput write(double value) {
        this.out.append(value);
        return this;
    }

    public String build() {
        return out.toString();
    }
}
