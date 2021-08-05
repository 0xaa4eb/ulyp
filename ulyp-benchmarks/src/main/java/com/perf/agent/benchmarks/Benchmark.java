package com.perf.agent.benchmarks;

import java.util.List;

public interface Benchmark {

    List<BenchmarkProfile> getProfiles();

    void setUp() throws Exception;

    void tearDown() throws Exception;

    void run() throws Exception;
}
