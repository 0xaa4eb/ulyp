package com.perf.agent.benchmarks.instrumentation;

import com.perf.agent.benchmarks.util.BenchmarkConstants;
import org.jetbrains.annotations.NotNull;
import org.openjdk.jmh.annotations.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
public class InstrumentationBenchmark {

    private static final int CLASSLOADER_COUNT = 10;
    private static final int CLASES_PER_CLASSLOADER = 100;

    @Fork(value = BenchmarkConstants.FORKS)
    @Benchmark
    public List<Class<?>> loadWithBaseline() {
        return loadClasses();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.ENABLE_AGENT_SYSTEM_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.DoesntExist.zxc,**.X000sadasd.vdmff,**.X10f0sadasd.vdmff,**.X50ssaadasd.vdmff,**.X510sadasd.vdmnsbd"
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public List<Class<?>> instrumentClasses() {
        return loadClasses();
    }

    @NotNull
    private static List<Class<?>> loadClasses() {
        URL[] urls = getClassURLs();

        List<Class<?>> classes = new ArrayList<>(CLASSLOADER_COUNT * CLASES_PER_CLASSLOADER);
        for (int loaderIdx = 0; loaderIdx < CLASSLOADER_COUNT; loaderIdx++) {

            ClassLoader classLoader = new URLClassLoader(urls, null);
            for (int clIdx = 0; clIdx < 100; clIdx++) {
                try {
                    classes.add(classLoader.loadClass("com.perf.agent.benchmarks.instrumentation.classes.X" + clIdx));
                } catch (Exception e) {
                    throw new RuntimeException("Class not found, test failed", e);
                }
            }
        }
        return classes;
    }

    @NotNull
    private static URL[] getClassURLs() {
        URL[] urls;
        try {
            URL url = Paths.get(".", "build", "classes", "java", "jmh").toFile().toURL();
            urls = new URL[]{url};
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return urls;
    }
}
