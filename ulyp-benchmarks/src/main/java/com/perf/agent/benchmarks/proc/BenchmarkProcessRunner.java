package com.perf.agent.benchmarks.proc;

import com.perf.agent.benchmarks.BenchmarkProfile;
import org.buildobjects.process.ProcBuilder;
import org.buildobjects.process.ProcResult;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BenchmarkProcessRunner {

    private static final boolean VERBOSE = true;

    public static void runClassInSeparateJavaProcess(Class<?> benchmarkClazz, BenchmarkProfile profile) {
        String classPath = System.getProperty("java.class.path");

        try {
            String javaHome = System.getProperty("java.home");
            String javaBinary = Paths.get(javaHome, "bin", "java").toString();

            List<String> processArgs = new ArrayList<>();

            processArgs.add("-cp");
            processArgs.add(classPath);
            processArgs.addAll(profile.getSubprocessCmdArgs());


            processArgs.add(benchmarkClazz.getName());

            ProcResult result = new ProcBuilder(javaBinary, processArgs.toArray(new String[]{}))
                    .withTimeoutMillis(TimeUnit.MINUTES.toMillis(3))
                    .ignoreExitStatus()
                    .run();

            if (VERBOSE) {
                System.out.println("Proc output:\n" + result.getOutputString());
                System.out.println("Proc err:\n" + result.getErrorString());
                System.out.println("Proc run time: " + result.getExecutionTime() + " ms");
            }

            if (result.getExitValue() != 0) {
                throw new RuntimeException("Process exit code is not 0, proc string " + result.getProcString());
            }
        } catch (Exception e) {
            throw new RuntimeException("Process ended unsuccessfully", e);
        }
    }
}
