package com.perf.agent.benchmarks;

import com.perf.agent.benchmarks.proc.BenchmarkEnv;
import com.perf.agent.benchmarks.proc.OutputFile;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.core.util.PackageList;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BenchmarkProfile {

    @Nullable
    private final MethodMatcher methodToRecord;
    @NotNull
    private final PackageList instrumentedPackages;
    private final List<String> additionalProcessArgs;
    private final OutputFile outputFile;
    private final boolean agentEnabled;

    public BenchmarkProfile(
            @Nullable MethodMatcher methodToRecord,
            @NotNull PackageList instrumentedPackages,
            List<String> additionalProcessArgs,
            OutputFile outputFile,
            boolean agentEnabled)
    {
        this.methodToRecord = methodToRecord;
        this.instrumentedPackages = instrumentedPackages;
        this.additionalProcessArgs = additionalProcessArgs;
        this.outputFile = outputFile;
        this.agentEnabled = agentEnabled;
    }

    public boolean shouldWriteRecording() {
        return outputFile != null && !instrumentedPackages.isEmpty() && methodToRecord != null;
    }

    public List<String> getSubprocessCmdArgs() {
        List<String> args = new ArrayList<>();

        if (agentEnabled) {
            args.add("-javaagent:" + BenchmarkEnv.findBuiltAgentJar());
        }

        if (!instrumentedPackages.isEmpty()) {
            args.add("-Dulyp.packages=" + this.instrumentedPackages);
        }

        args.add("-Dulyp.file=" + (outputFile != null ? outputFile.toString() : ""));

        if (methodToRecord != null) {
            args.add("-Dulyp.methods=" + Objects.requireNonNull(this.methodToRecord));
        }


        args.addAll(additionalProcessArgs);
        return args;
    }

    public OutputFile getOutputFile() {
        return outputFile;
    }

    @Override
    public String toString() {
        return "Agent: " + (agentEnabled ? "Y" : "N") +
                "/" + (agentEnabled ? (instrumentedPackages.isEmpty() ? "*" : instrumentedPackages) : "-")  +
                "/" + (methodToRecord != null ? methodToRecord : (agentEnabled ? "main method" : "-"));
    }
}
