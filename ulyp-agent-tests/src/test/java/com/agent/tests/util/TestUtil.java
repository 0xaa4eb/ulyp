package com.agent.tests.util;

import org.buildobjects.process.ProcBuilder;
import org.buildobjects.process.ProcResult;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestUtil {

    private static final Pattern JDK_VERSION_PATTERN = Pattern.compile("(\\d\\d)\\..*");
    private static final int OPEN_MODULES_JDK_VERSION = 17;

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
            openJavaModules(processArgs);
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

    private static void openJavaModules(List<String> processArgs) {
        String jdkVersionRaw = System.getProperty("java.version");
        Matcher matcher = JDK_VERSION_PATTERN.matcher(jdkVersionRaw);
        if (matcher.matches()) {
            int jdkVersion;
            try {
                jdkVersion = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException numberFormatException) {
                throw new AssertionFailedError("Could not define java version from " + jdkVersionRaw);
            }

            if (jdkVersion >= OPEN_MODULES_JDK_VERSION) {
                processArgs.add("--add-opens=java.base/java.lang=ALL-UNNAMED");
                processArgs.add("--add-opens=java.base/java.lang.invoke=ALL-UNNAMED");
                processArgs.add("--add-opens=java.base/sun.nio.ch=ALL-UNNAMED");
            }
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
