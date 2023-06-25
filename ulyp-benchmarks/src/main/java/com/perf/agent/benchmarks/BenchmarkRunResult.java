package com.perf.agent.benchmarks;

import com.ulyp.core.util.ByteSize;
import org.HdrHistogram.Histogram;

public class BenchmarkRunResult {

    private final Class<?> benchmarkClazz;
    private final BenchmarkScenario scenario;
    private final Histogram procTimeHistogram;
    private final Histogram recordTimeHistogram;
    private final Histogram recordedCallsCountHistogram;
    private final Histogram recordingsCountHistogram;
    private final Histogram outputFileSizeHistogram;

    public BenchmarkRunResult(
            Class<?> benchmarkClazz,
            BenchmarkScenario scenario,
            Histogram procTimeHistogram,
            Histogram recordTimeHistogram,
            Histogram recordedCallsCountHistogram,
            Histogram recordingsCountHistogram,
            Histogram outputFileSizeHistogram) {
        this.benchmarkClazz = benchmarkClazz;
        this.scenario = scenario;
        this.procTimeHistogram = procTimeHistogram;
        this.recordTimeHistogram = recordTimeHistogram;
        this.recordedCallsCountHistogram = recordedCallsCountHistogram;
        this.recordingsCountHistogram = recordingsCountHistogram;
        this.outputFileSizeHistogram = outputFileSizeHistogram;
    }

    public void print() {
        StringBuilder scenarioBuilder = new StringBuilder();

        scenarioBuilder.append(benchmarkClazz.getSimpleName());
        scenarioBuilder.append(": ");
        padTo(scenarioBuilder, 35);
        scenarioBuilder.append(scenario);

        StringBuilder msrmentBuilder = new StringBuilder();
        msrmentBuilder.append("\t\t")
                .append(String.format("%.2f", procTimeHistogram.getMean() / 1000.0))
                .append(" ± ")
                .append(String.format("%.3f", procTimeHistogram.getStdDeviation() / 1000.0))
                .append(" s");
        padTo(msrmentBuilder, 30);

        msrmentBuilder.append(String.format("%.2f", recordTimeHistogram.getMean() / 1000.0))
                .append(" ± ")
                .append(String.format("%.3f", recordTimeHistogram.getStdDeviation() / 1000.0))
                .append(" s");
        padTo(msrmentBuilder, 60);

        msrmentBuilder.append("  ")
                .append("    calls=")
                .append((int) recordedCallsCountHistogram.getMean());
        padTo(msrmentBuilder, 90);

        msrmentBuilder.append("    recs=")
                .append((int) recordingsCountHistogram.getMean());
        padTo(msrmentBuilder, 120);

        msrmentBuilder.append("    file sz=")
                .append(ByteSize.toHumanReadable(Math.round(outputFileSizeHistogram.getMean())))
                .append(" ± ")
                .append(ByteSize.toHumanReadable(Math.round(outputFileSizeHistogram.getStdDeviation())));

        System.out.println(scenarioBuilder);
        System.out.println(msrmentBuilder);
    }

    private void padTo(StringBuilder builder, int length) {
        while (builder.length() < length) {
            builder.append(' ');
        }
    }
}
