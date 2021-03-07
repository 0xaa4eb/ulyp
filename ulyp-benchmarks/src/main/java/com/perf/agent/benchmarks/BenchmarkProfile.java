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

    public BenchmarkProfile(
            @Nullable MethodMatcher methodToRecord,
            @NotNull PackageList instrumentedPackages,
            List<String> additionalProcessArgs,
            OutputFile outputFile)
    {
        this.methodToRecord = methodToRecord;
        this.instrumentedPackages = instrumentedPackages;
        this.additionalProcessArgs = additionalProcessArgs;
        this.outputFile = outputFile;
    }

    public boolean shouldWriteRecording() {
        return outputFile != null && !instrumentedPackages.isEmpty() && methodToRecord != null;
    }

    public List<String> getSubprocessCmdArgs() {
        List<String> args = new ArrayList<>();
        if (!instrumentedPackages.isEmpty()) {
            args.add("-javaagent:" + BenchmarkEnv.findBuiltAgentJar());
        }
        args.add("-Dulyp.file=" + (outputFile != null ? outputFile.toString() : ""));
        args.add("-Dulyp.methods=" + Objects.requireNonNull(this.methodToRecord));
        args.add("-Dulyp.packages=" + this.instrumentedPackages);

        args.addAll(additionalProcessArgs);

        return args;
    }

    public OutputFile getOutputFile() {
        return outputFile;
    }

    @Override
    public String toString() {
        if (!instrumentedPackages.isEmpty()) {
            return instrumentedPackages + "/" + (methodToRecord != null ? methodToRecord : "no tracing");
        } else {
            return "no agent";
        }
    }
}
