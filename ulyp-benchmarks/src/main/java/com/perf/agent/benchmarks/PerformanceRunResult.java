package com.perf.agent.benchmarks;

import org.HdrHistogram.Histogram;

public class PerformanceRunResult {

    private final Class<?> benchmarkClazz;
    private final BenchmarkProfile profile;
    private final Histogram procTimeHistogram;
    private final Histogram recordTimeHistogram;
    private final Histogram recordsCountHistogram;

    public PerformanceRunResult(
            Class<?> benchmarkClazz,
            BenchmarkProfile profile,
            Histogram procTimeHistogram,
            Histogram recordTimeHistogram,
            Histogram recordsCountHistogram) {
        this.benchmarkClazz = benchmarkClazz;
        this.profile = profile;
        this.procTimeHistogram = procTimeHistogram;
        this.recordTimeHistogram = recordTimeHistogram;
        this.recordsCountHistogram = recordsCountHistogram;
    }

    public void print() {
        StringBuilder builder = new StringBuilder();

        builder.append(benchmarkClazz.getSimpleName());
        builder.append(": ");
        padTo(builder, 30);
        builder.append(profile);
        padTo(builder, 70);

        builder.append(String.format("%.2f", procTimeHistogram.getMean() / 1000.0))
                .append(" ± ")
                .append(String.format("%.3f", procTimeHistogram.getStdDeviation() / 1000.0))
                .append("   ")
                .append(String.format("%.2f", recordTimeHistogram.getMean() / 1000.0))
                .append(" ± ")
                .append(String.format("%.3f", recordTimeHistogram.getStdDeviation() / 1000.0))
                .append("   ")
                .append("sec")
                .append(" ")
                .append(recordsCountHistogram.getMean());

        System.out.println(builder);
    }

    private void padTo(StringBuilder builder, int length) {
        while(builder.length() < length) {
            builder.append(' ');
        }
    }
}
