package com.agent.tests.util;

import org.buildobjects.process.ProcBuilder;
import org.buildobjects.process.ProcResult;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TestUtil {

    public static void runClassInSeparateJavaProcess(ForkProcessBuilder settingsBuilder) {
        File agentJar = findAgentJar();
        String classPath = System.getProperty("java.class.path");

        try {
            String javaHome = System.getProperty("java.home");
            String javaBinary = Paths.get(javaHome, "bin", "java").toString();

            List<String> processArgs = new ArrayList<>();
            processArgs.add("-javaagent:" + agentJar.getAbsolutePath());
            processArgs.add("-cp");
            processArgs.add(classPath);
            processArgs.addAll(settingsBuilder.toCmdJavaProps());
            processArgs.add(settingsBuilder.getMainClass().getName());

            ProcResult result = new ProcBuilder(javaBinary, processArgs.toArray(new String[]{}))
                    .withTimeoutMillis(TimeUnit.MINUTES.toMillis(3))
                    .ignoreExitStatus()
                    .run();

            System.out.println("Proc output:\n" + result.getOutputString());
            System.out.println("Proc err:\n" + result.getErrorString());
            System.out.println("Proc run time: " + result.getExecutionTime() + " ms");

            if (result.getExitValue() != 0) {
                Assertions.fail("Process exit code is not 0, proc string " + result.getProcString());
            }
        } catch (Exception e) {
            throw new AssertionError("Process ended unsuccessfully", e);
        }
    }

    private static File findAgentJar() {
        Path libDir = Paths.get("..", "ulyp-agent", "build", "libs");

        return Arrays.stream(Objects.requireNonNull(libDir.toFile().listFiles()))
                .filter(file -> file.getName().startsWith("ulyp-agent"))
                .filter(file -> file.getName().endsWith(".jar"))
                .findAny()
                .orElseThrow(() -> new AssertionError("Could not find built ulyp agent jar"));
    }
}
