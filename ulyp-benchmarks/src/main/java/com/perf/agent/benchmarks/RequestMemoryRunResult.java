package com.perf.agent.benchmarks;

public class RequestMemoryRunResult {

    private final Class<?> benchmarkClazz;
    private final BenchmarkProfile profile;
    private final long bytesSize;

    public RequestMemoryRunResult(
            Class<?> benchmarkClazz,
            BenchmarkProfile profile,
            long bytesSize) {
        this.benchmarkClazz = benchmarkClazz;
        this.profile = profile;
        this.bytesSize = bytesSize;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(benchmarkClazz.getSimpleName());
        builder.append(": ");
        padTo(builder, 50);
        builder.append(profile);
        padTo(builder, 100);

        builder.append(bytesSize / 1000).append(" kb");

        return builder.toString();
    }

    private void padTo(StringBuilder builder, int length) {
        while (builder.length() < length) {
            builder.append(' ');
        }
    }
}
