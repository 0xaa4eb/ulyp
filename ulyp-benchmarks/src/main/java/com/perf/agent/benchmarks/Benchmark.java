package com.perf.agent.benchmarks;

import java.util.List;

public interface Benchmark {

    List<BenchmarkScenario> getProfiles();

    default void setUp() throws Exception {

    }

    default void tearDown() throws Exception {

    }

    default void run() throws Exception {

    }
}
