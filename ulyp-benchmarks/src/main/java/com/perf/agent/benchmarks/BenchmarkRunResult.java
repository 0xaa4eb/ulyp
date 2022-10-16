package com.perf.agent.benchmarks;

import org.HdrHistogram.Histogram;

public class BenchmarkRunResult {

    private final Class<?> benchmarkClazz;
    private final BenchmarkScenario profile;
    private final Histogram procTimeHistogram;
    private final Histogram recordTimeHistogram;
    private final Histogram recordedCallsCountHistogram;
    private final Histogram recordingsCountHistogram;

    public BenchmarkRunResult(
            Class<?> benchmarkClazz,
            BenchmarkScenario profile,
            Histogram procTimeHistogram,
            Histogram recordTimeHistogram,
            Histogram recordedCallsCountHistogram,
            Histogram recordingsCountHistogram) {
        this.benchmarkClazz = benchmarkClazz;
        this.profile = profile;
        this.procTimeHistogram = procTimeHistogram;
        this.recordTimeHistogram = recordTimeHistogram;
        this.recordedCallsCountHistogram = recordedCallsCountHistogram;
        this.recordingsCountHistogram = recordingsCountHistogram;
    }

    public void print() {
        StringBuilder builder = new StringBuilder();

        builder.append(benchmarkClazz.getSimpleName());
        builder.append(": ");
        padTo(builder, 35);
        builder.append(profile);
        padTo(builder, 135);

        builder.append(String.format("%.2f", procTimeHistogram.getMean() / 1000.0))
                .append(" ± ")
                .append(String.format("%.3f", procTimeHistogram.getStdDeviation() / 1000.0))
                .append("     ")
                .append(String.format("%.2f", recordTimeHistogram.getMean() / 1000.0))
                .append(" ± ")
                .append(String.format("%.3f", recordTimeHistogram.getStdDeviation() / 1000.0))
                .append("  ")
                .append("sec")
                .append("    ")
                .append((int) recordedCallsCountHistogram.getMean())
                .append("    ")
                .append((int) recordingsCountHistogram.getMean());

        System.out.println(builder);
    }

    private void padTo(StringBuilder builder, int length) {
        while (builder.length() < length) {
            builder.append(' ');
        }
    }
}
