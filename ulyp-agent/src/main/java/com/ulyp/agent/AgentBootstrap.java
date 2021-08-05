package com.ulyp.agent;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AgentBootstrap {

    private static final Class<?> thisClass = AgentBootstrap.class;

    public static void premain(String args, Instrumentation instrumentation) {
        try {
            instrumentation.appendToBootstrapClassLoaderSearch(
                    new JarFile(findClassesJar())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Agent.start(args, instrumentation);
    }

    private static File findClassesJar() {
        Path agentJarDir = findThisAgentJar().toPath().getParent().toAbsolutePath();

        while (!agentJarDir.getParent().equals(agentJarDir)) {

            // TODO remove version from here
            if (Files.exists(Paths.get(agentJarDir.toString(), "ulyp-agent-classes-0.2.jar"))) {
                return Paths.get(agentJarDir.toString(), "ulyp-agent-classes-0.2.jar").toFile();
            }

            Path classesJarPath = Paths.get(agentJarDir.toString(), "ulyp-agent-classes", "build", "libs", "ulyp-agent-classes-0.2.jar");
            if (Files.exists(classesJarPath)) {
                return classesJarPath.toFile();
            }

            agentJarDir = agentJarDir.getParent();
        }

        throw new RuntimeException("Could not find classes jar based on agent path " + agentJarDir);
    }

    private static File findThisAgentJar() {
        List<String> arguments = getVMArgumentsThroughReflection();

        String agentArgument = null;
        for (final String arg : arguments) {
            if (arg.startsWith("-javaagent") && arg.contains("ulyp")) {
                agentArgument = arg;
            }
        }

        if (agentArgument == null) {
            throw new RuntimeException(
                    "Could not find javaagent parameter and code source unavailable, " +
                    "not installing tracing agent, arguments = " + arguments);
        }

        // argument is of the form -javaagent:/path/to/dd-java-agent.jar=optionalargumentstring
        final Matcher matcher = Pattern.compile("-javaagent:([^=]+).*").matcher(agentArgument);

        if (!matcher.matches()) {
            throw new RuntimeException("Unable to parse javaagent parameter: " + agentArgument);
        }

        final File javaagentFile = new File(matcher.group(1));
        if (!(javaagentFile.exists() || javaagentFile.isFile())) {
            throw new RuntimeException("Unable to find javaagent file: " + javaagentFile);
        }

        return javaagentFile;
    }

    private static List<String> getVMArgumentsThroughReflection() {
        try {
            // Try Oracle-based
            final Class<?> managementFactoryHelperClass =
                    thisClass.getClassLoader().loadClass("sun.management.ManagementFactoryHelper");

            final Class<?> vmManagementClass =
                    thisClass.getClassLoader().loadClass("sun.management.VMManagement");

            Object vmManagement;

            try {
                vmManagement =
                        managementFactoryHelperClass.getDeclaredMethod("getVMManagement").invoke(null);
            } catch (final NoSuchMethodException e) {
                // Older vm before getVMManagement() existed
                final Field field = managementFactoryHelperClass.getDeclaredField("jvm");
                field.setAccessible(true);
                vmManagement = field.get(null);
                field.setAccessible(false);
            }

            return (List<String>) vmManagementClass.getMethod("getVmArguments").invoke(vmManagement);

        } catch (final ReflectiveOperationException e) {
            try { // Try IBM-based.
                final Class<?> VMClass = thisClass.getClassLoader().loadClass("com.ibm.oti.vm.VM");
                final String[] argArray = (String[]) VMClass.getMethod("getVMArgs").invoke(null);
                return Arrays.asList(argArray);
            } catch (final ReflectiveOperationException e1) {
                // Fallback to default
                System.out.println(
                        "WARNING: Unable to get VM args through reflection.  A custom java.util.logging.LogManager may not work correctly");

                return ManagementFactory.getRuntimeMXBean().getInputArguments();
            }
        }
    }
}
