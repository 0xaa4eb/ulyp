package com.perf.agent.benchmarks.proc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

public class BenchmarkEnv {

    public static File findBuiltAgentJar() {
        Path libDir;
        if (Files.exists(Paths.get("..", "ulyp-agent"))) {
            libDir = Paths.get("..", "ulyp-agent", "build", "libs");
        } else {
            libDir = Paths.get("ulyp-agent", "build", "libs");
        }


        return Arrays.stream(Objects.requireNonNull(libDir.toFile().listFiles()))
                .filter(file -> file.getName().startsWith("ulyp-agent"))
                .filter(file -> file.getName().endsWith(".jar"))
                .findAny()
                .orElseThrow(() -> new AssertionError("Could not find built ulyp agent jar"));
    }
}
