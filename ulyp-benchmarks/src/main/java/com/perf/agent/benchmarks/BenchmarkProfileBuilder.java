package com.perf.agent.benchmarks;

import com.perf.agent.benchmarks.proc.BenchmarkEnv;
import com.perf.agent.benchmarks.proc.OutputFile;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.core.util.PackageList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BenchmarkProfileBuilder {

    private MethodMatcher methodToRecord;
    @NotNull
    private PackageList instrumentedPackages = new PackageList();
    private OutputFile outputFile = new OutputFile("ulyp-benchmark", "dat");
    private final List<String> additionalProcessArgs = new ArrayList<>();

    public BenchmarkProfileBuilder withAdditionalArgs(String... args) {
        additionalProcessArgs.addAll(Arrays.asList(args));
        return this;
    }

    public BenchmarkProfileBuilder withMethodToRecord(MethodMatcher methodToRecord) {
        this.methodToRecord = methodToRecord;
        return this;
    }

    public BenchmarkProfileBuilder withInstrumentedPackages(@NotNull PackageList instrumentedPackages) {
        this.instrumentedPackages = instrumentedPackages;
        return this;
    }

    public BenchmarkProfileBuilder withUiDisabled() {
        outputFile = null;
        return this;
    }

    public BenchmarkProfile build() {
        return new BenchmarkProfile(methodToRecord, instrumentedPackages, additionalProcessArgs, outputFile);
    }
}
